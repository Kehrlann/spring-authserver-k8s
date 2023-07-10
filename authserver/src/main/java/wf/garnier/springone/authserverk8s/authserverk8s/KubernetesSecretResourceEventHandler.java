package wf.garnier.springone.authserverk8s.authserverk8s;

import io.kubernetes.client.informer.ResourceEventHandler;
import io.kubernetes.client.openapi.models.V1Secret;

class KubernetesSecretResourceEventHandler implements ResourceEventHandler<V1Secret> {

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
