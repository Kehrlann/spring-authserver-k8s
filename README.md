# spring-authserver-k8s

Spring Authserver on Kubernetes

## Usage

### Initial set up

We will setup a KinD cluster, with Ingress enabled.

- Install KinD (`brew install kind`)
  - This will be your local kubernetes cluster
- Install kapp, part of Carvel (`brew tap vmware-tanzu/carvel && brew install kapp`)
  - This is used as part of the installation process

### Build and deploy the app

- Install Tilt (`brew install tilt`)
  - This is used for orchestrating the build / deploy process on KinD
- Install ytt, part of Carvel (`brew tap vmware-tanzu/carvel && brew install ytt`)
  - ytt is used to generate Secrets that contain authserver JWK signing keys
- Run `tilt up`
  - This will build your java app, and deploy it to kind

Validate that the deployment works as expected:
- Visit `http://authserver.127.0.0.1.nip.io`
- Visit `http://authserver.127.0.0.1.nip.io/oauth2/jwks`, you should see a single JWK
- Get a token using `./utils/get-token.sh`

### Rotate JWKs

- Generate a new key using `KEY_USAGE=signing ./utils/generate-key.sh`
  - This key has label `security.spring.io/key-usage: signing` and can be used for signing JWTs
- Apply this key to the cluster with `kubectl apply`
- You may now delete the default key that was created for you:
  - `kubectl delete secret default-key -n authserver`
