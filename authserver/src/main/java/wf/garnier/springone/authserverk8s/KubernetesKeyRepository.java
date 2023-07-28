package wf.garnier.springone.authserverk8s;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.RSAKey;
import io.kubernetes.client.informer.ResourceEventHandler;
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

public final class KubernetesKeyRepository implements KeyRepository {
    private final Lister<V1Secret> secrets;

    public KubernetesKeyRepository() throws IOException {
        this.secrets = setupSecretLister();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Key> T findKey(String id) {
        for (Key key : getKeys()) {
            if (key.getId().equals(id)) {
                return (T) key;
            }
        }
        return null;
    }

    @Override
    public List<? extends Key> getKeys() {
        List<? extends Key> result = getConvertedKeys();
        result.sort(Comparator.comparing(Key::getCreated).reversed());
        return result;
    }

    @Override
    public void delete(String id) {
        // No-op
    }

    @Override
    public <T extends Key> void save(T key) {
        // No-op
    }

    private List<? extends Key> getConvertedKeys() {
        List<Key> keys = new ArrayList<>();

        this.secrets.list().forEach(secret -> {
            JWK jwk = parseJwk(secret);
            if (jwk instanceof RSAKey rsaJwk) {
                try {
                    KeyRepository.AsymmetricKey asymmetricKey = new KeyRepository.AsymmetricKey(
                            secret.getMetadata().getName(),
                            secret.getMetadata().getCreationTimestamp().toInstant(),
                            rsaJwk.toPrivateKey(),
                            rsaJwk.toPublicKey());
                    keys.add(asymmetricKey);
                } catch (Exception ignored) {
                }
            }
        });

        return keys;
    }

    private static JWK parseJwk(V1Secret secret) {
        try {
            return JWK.parseFromPEMEncodedObjects(new String(secret.getData().get("key.pem")));
        } catch (Exception ignored) {
            return null;
        }
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

    private static class KubernetesSecretResourceEventHandler implements ResourceEventHandler<V1Secret> {

        @Override
        public void onAdd(V1Secret obj) {
            System.out.printf("~~~~~~~> ADDED [%s]%n", obj.getMetadata().getName());
        }

        @Override
        public void onUpdate(V1Secret oldObj, V1Secret newObj) {
            System.out.printf("~~~~~~~> UPDATED [%s]%n", newObj.getMetadata().getName());
        }

        @Override
        public void onDelete(V1Secret obj, boolean deletedFinalStateUnknown) {
            System.out.printf("~~~~~~~> DELETED [%s]%n", obj.getMetadata().getName());
        }
    }

}