# spring-authserver-k8s

Securing Web Apps on Kubernetes with Spring Authorization Server.

## Pre-requisites

- [KinD](kind.sigs.k8s.io/)
- [Tilt](http://tilt.dev/)
- [YTT from Carvel](https://carvel.dev/ytt/)
- [KAPP from Carvel](https://carvel.dev/kapp/)
- `docker`
- The `openssl` and `kubectl` CLIs
- Java 17

## Part 1: Custom Authorization Server

### Initial set up

The demo runs on a KinD cluster, with an NGINX Ingress controller. It relies on `nip.io` for domain
resolution (so that e.g. `authserver.127.0.0.1.nip.io` resolves to `127.0.0.1`).

Run `setup.sh` to install a `KinD` cluster named `spring-authserver`, with all required
dependencies.

**Note**: we override some DNS config within the cluster, check out `coredns-cm.yml` for details.

### Build and deploy both the Auth Server and the SSO client

Run `tilt up`. This will build both application and deploy them to your local KinD cluster.

Validate that the deployment works as expected:
- Visit `http://authserver.127.0.0.1.nip.io`
- Visit `http://authserver.127.0.0.1.nip.io/oauth2/jwks`, you should see a single JWK
- Get a token using `./utils/get-token.sh`
- Visit `http://sso-client.127.0.0.1.nip.io` and do a full login flow, with user `user1` and
  password `password`.

### Rotate JWKs

- Generate a new key using `./utils/generate-key.sh`
- Apply this key to the cluster with `./utils/generate-key.sh | kubectl apply -f -`
  or directly with `./utils/apply-new-key.sh`
- You may now delete the default key that was created for you:
  - `kubectl delete secret default-key -n authserver`

## Part 2: AppSSO and Tanzu Developer Sandbox

In the demo we showcase a complex scenario, with all sources available in the `/tap`
directory. Those sources can not be applied to a Tanzu Developer Sandbox directly.

Instead, use the `/tap-sandbox` directory. There is a README with specific instructions.
