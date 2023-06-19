package wf.garnier.spring.authserverk8s;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import io.kubernetes.client.informer.ResourceEventHandler;
import io.kubernetes.client.informer.SharedIndexInformer;
import io.kubernetes.client.informer.SharedInformerFactory;
import io.kubernetes.client.informer.cache.Lister;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1Secret;
import io.kubernetes.client.openapi.models.V1SecretList;
import io.kubernetes.client.util.CallGeneratorParams;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.Namespaces;
import okhttp3.OkHttpClient;

class KubernetesJwkRepository implements JWKSource<SecurityContext> {

    private final Lister<V1Secret> allKeys;

    public KubernetesJwkRepository() throws IOException, InterruptedException {
        SharedIndexInformer<V1Secret> allKeysInformer = setupKubernetesInformer();
        allKeys = new Lister<>(allKeysInformer.getIndexer(), getNamespace());
    }

    @Override
    public List<JWK> get(JWKSelector jwkSelector, SecurityContext context) {
        boolean keyUsageIsSignature = jwkSelector.getMatcher().getKeyUses() != null
                && jwkSelector.getMatcher().getKeyUses().contains(KeyUse.SIGNATURE);

        var keysFromKubernetes = allKeys
                .list()
                .stream()
                .map(KubernetesJwkRepository::parseJwk)
                .flatMap(Optional::stream);
        if (keyUsageIsSignature) {
            return keysFromKubernetes.filter(k -> KeyUse.SIGNATURE.equals(k.getKeyUse()))
                    .limit(1)
                    .toList();
        } else {
            return keysFromKubernetes.toList();
        }
    }

    private static Optional<JWK> parseJwk(V1Secret secret) {
        var name = secret.getMetadata().getName();
        try {
            JWK parsed = JWK.parseFromPEMEncodedObjects(new String(secret.getData().get("key.pem")));
            return Optional.of(
                    new RSAKey.Builder(parsed.toRSAKey())
                            .keyID(name)
                            .keyUse(getKeyUse(secret))
                            .build()
            );
        } catch (JOSEException ignored) {
        }
        return Optional.empty();
    }

    private static KeyUse getKeyUse(V1Secret secret) {
        return Optional.ofNullable(secret.getMetadata())
                .map(V1ObjectMeta::getLabels)
                .map(l -> l.get("security.spring.io/key-usage"))
                .filter("signing"::equals)
                .map(k -> KeyUse.SIGNATURE)
                .orElse(null);
    }


    @Nonnull
    private static SharedIndexInformer<V1Secret> setupKubernetesInformer() throws IOException, InterruptedException {
        final String finalPodNamespace = getNamespace();
        var apiClient = ClientBuilder.defaultClient();
        var coreV1Api = new CoreV1Api(apiClient);
        OkHttpClient httpClient =
                apiClient.getHttpClient().newBuilder().readTimeout(0, TimeUnit.SECONDS).build();
        apiClient.setHttpClient(httpClient);
        var informerFactory = new SharedInformerFactory(apiClient);

        var allKeysInformer = informerFactory.sharedIndexInformerFor(
                (CallGeneratorParams params) ->
                        coreV1Api.listNamespacedSecretCall(
                                finalPodNamespace,
                                null,
                                null,
                                null,
                                "type=authserver/jwk",
                                null,
                                null,
                                params.resourceVersion,
                                null,
                                params.timeoutSeconds,
                                params.watch,
                                null
                        ),
                V1Secret.class,
                V1SecretList.class
        );


        allKeysInformer.addEventHandler(new ResourceEventHandler<>() {
            @Override
            public void onAdd(V1Secret obj) {
                System.out.println(message(obj, "ADDED"));
            }

            @Override
            public void onUpdate(V1Secret oldObj, V1Secret newObj) {
                System.out.println(message(newObj, "UPDATED"));
            }

            @Override
            public void onDelete(V1Secret obj, boolean deletedFinalStateUnknown) {
                System.out.println(message(obj, "DELETED"));
            }

            private String message(V1Secret obj, String method) {
                var name = obj.getMetadata().getName();
                var usage = Optional.ofNullable(obj.getMetadata())
                        .map(V1ObjectMeta::getLabels)
                        .map(l -> l.get("security.spring.io/key-usage"))
                        .orElse("unset");
                return ("~~~~~~> %s name=[%s] security.spring.io/key-usage=[%s]").formatted(method, name, usage);
            }
        });

        informerFactory.startAllRegisteredInformers();
        for (int i = 0; i < 10; i++) {
            if (allKeysInformer.hasSynced()) {
                break;
            }
            Thread.sleep(100);
        }
        return allKeysInformer;
    }

    private static String getNamespace() {
        String podNamespace;
        try {
            podNamespace = Namespaces.getPodNamespace();
        } catch (IOException e) {
            podNamespace = "default";
        }
        return podNamespace;
    }

}
