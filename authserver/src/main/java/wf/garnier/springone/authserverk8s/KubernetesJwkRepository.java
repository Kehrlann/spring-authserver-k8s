package wf.garnier.springone.authserverk8s;

import java.io.IOException;
import java.util.Comparator;
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
import io.kubernetes.client.informer.SharedIndexInformer;
import io.kubernetes.client.informer.SharedInformerFactory;
import io.kubernetes.client.informer.cache.Lister;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Secret;
import io.kubernetes.client.openapi.models.V1SecretList;
import io.kubernetes.client.util.CallGeneratorParams;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.Namespaces;
import okhttp3.OkHttpClient;

class KubernetesJwkRepository implements JWKSource<SecurityContext> {

    private final Lister<V1Secret> allKeys;

    public KubernetesJwkRepository() throws IOException {
        allKeys = setupSecretLister();
    }

    @Override
    public List<JWK> get(JWKSelector jwkSelector, SecurityContext context) {
        boolean keyUsageIsSignature = jwkSelector.getMatcher().getKeyUses() != null
                && jwkSelector.getMatcher().getKeyUses().contains(KeyUse.SIGNATURE);

        var keysFromKubernetes = allKeys
                .list()
                .stream()
                .sorted(Comparator.comparing((V1Secret s) -> s.getMetadata().getCreationTimestamp()).reversed())
                .map(KubernetesJwkRepository::parseJwk)
                .flatMap(Optional::stream);
        if (keyUsageIsSignature) {
            return keysFromKubernetes
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
            var rsaKey = new RSAKey.Builder(parsed.toRSAKey()).keyID(name).build();
            return Optional.of(rsaKey);
        } catch (JOSEException ignored) {
        }
        return Optional.empty();
    }

    @Nonnull
    private static Lister<V1Secret> setupSecretLister() throws IOException {
        final String namespace = Namespaces.getPodNamespace();

        var apiClient = ClientBuilder.defaultClient();
        var coreV1Api = new CoreV1Api(apiClient);
        // TODO: required?
        OkHttpClient httpClient =
                apiClient.getHttpClient().newBuilder().readTimeout(0, TimeUnit.SECONDS).build();
        apiClient.setHttpClient(httpClient);
        // end TODO
        var informerFactory = new SharedInformerFactory(apiClient);

        var allKeysInformer = informerFactory.sharedIndexInformerFor(
                (CallGeneratorParams params) ->
                        coreV1Api.listNamespacedSecretCall(
                                namespace,
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


        allKeysInformer.addEventHandler(new KubernetesSecretResourceEventHandler());

        informerFactory.startAllRegisteredInformers();
        // Optional, in our demo it will sync very fast
        // waitForInformerSync(allKeysInformer);
        return new Lister<>(allKeysInformer.getIndexer(), Namespaces.getPodNamespace());
    }

    // Optional
    private static void waitForInformerSync(SharedIndexInformer<V1Secret> allKeysInformer) throws InterruptedException {
        for (int i = 0; i < 10; i++) {
            if (allKeysInformer.hasSynced()) {
                break;
            }
            Thread.sleep(100);
        }
    }

}
