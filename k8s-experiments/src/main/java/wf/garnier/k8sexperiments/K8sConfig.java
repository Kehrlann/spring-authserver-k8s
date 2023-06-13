package wf.garnier.k8sexperiments;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import io.kubernetes.client.informer.ResourceEventHandler;
import io.kubernetes.client.informer.SharedIndexInformer;
import io.kubernetes.client.informer.SharedInformerFactory;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Secret;
import io.kubernetes.client.openapi.models.V1SecretList;
import io.kubernetes.client.util.CallGeneratorParams;
import io.kubernetes.client.util.ClientBuilder;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;

@Configuration
class K8sConfig implements InitializingBean {

    private static String podNamespace = "default";

    @Override
    public void afterPropertiesSet() throws Exception {
        listSecrets("type=authserver/jwk");
        informMe();
    }

    private static void listSecrets(String fieldSelector) throws IOException, ApiException {
        var apiClient = ClientBuilder.defaultClient();
        var coreV1Api = new CoreV1Api(apiClient);
        V1SecretList secrets = coreV1Api.listNamespacedSecret(
                podNamespace,
                null,
                null,
                null,
                fieldSelector, // TODO: field selector
                null, // TODO: label selector
                null,
                null,
                null,
                null,
                false
        );
        System.out.println("🔐 GOT %s SECRETS".formatted(secrets.getItems().size()));

        for (V1Secret item : secrets.getItems()) {
            System.out.println("  " + item.getMetadata().getName());
        }
    }

    private static void informMe() throws IOException {
        var apiClient = ClientBuilder.defaultClient();
        CoreV1Api coreV1Api = new CoreV1Api(apiClient);
        OkHttpClient httpClient =
                apiClient.getHttpClient().newBuilder().readTimeout(0, TimeUnit.SECONDS).build();
        apiClient.setHttpClient(httpClient);

        SharedInformerFactory factory = new SharedInformerFactory(apiClient);

        // Node informer
        SharedIndexInformer<V1Secret> nodeInformer =
                factory.sharedIndexInformerFor(
                        // **NOTE**:
                        // The following "CallGeneratorParams" lambda merely generates a stateless
                        // HTTPs requests, the effective apiClient is the one specified when constructing
                        // the informer-factory.
                        (CallGeneratorParams params) -> {
                            return coreV1Api.listNamespacedSecretCall(
                                    "default",
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    params.resourceVersion,
                                    null,
                                    params.timeoutSeconds,
                                    params.watch,
                                    null);
                        },
                        V1Secret.class,
                        V1SecretList.class);

        nodeInformer.addEventHandler(
                new ResourceEventHandler<V1Secret>() {
                    @Override
                    public void onAdd(V1Secret node) {
                        System.out.printf("%s node added!\n", node.getMetadata().getName());
                    }

                    @Override
                    public void onUpdate(V1Secret oldNode, V1Secret newNode) {
                        System.out.printf(
                                "%s => %s node updated!\n",
                                oldNode.getMetadata().getName(), newNode.getMetadata().getName());
                    }

                    @Override
                    public void onDelete(V1Secret node, boolean deletedFinalStateUnknown) {
                        System.out.printf("%s node deleted!\n", node.getMetadata().getName());
                    }
                });

        factory.startAllRegisteredInformers();
    }
}
