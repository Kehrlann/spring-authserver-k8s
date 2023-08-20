# Running the sso-client on TAP

This is a simplified version of the demo given at the SpringOne session "Securing Web Apps on
Kubernetes with Spring Authorization Server".


## Running on Tanzu Developer Sandbox

The code in this branch is designed to run on a Tanzu Developer Sandbox. Please note that the
default Sandbox runs with Developer permissions only, so users are not allowed to create
`AuthServer` resources, or interact with anything outside of the `apps` namespace.

As a result, this demo only creates one `ClientRegistration`, one `Workload` and one `ClassClaim`.

You may request a Tanzu Developer Sandbox on
[Tanzu Academy](https://tanzu.academy/guides/developer-sandbox).


## Prerequisites

- Access to a Developer Sandbox
- `kubectl`
- (optional) `curl`, `jq` and `base64` to run the `get-token.sh` script


## Overall architecture

The Developer Sandbox has an `AuthServer` provisioned, in the form of a `ClusterUnsafeTestLogin`, which you can discover by running:

```
tanzu services classes list
```


## Using a ClientRegistration

Typically a `ServiceClass` is used through `ClassClaims`, but for using AppSSO directly you can
apply a `ClientRegistration` directly:

```
kubectl apply --filename clientregistration.yml
```

Once it is applied, you can inspect it:

```
kubectl describe clientregistration demo-client
```

You can use the `ClientRegistration` by reading the provisioned `Secret`. More conveniently, you can
use the `get-token.sh` script:

```
./utils/get-token.sh
```


## Securing a Workload

Apply both the `Workload` and the `ClassClaim`:

```
kubectl apply --filename workload.yml --filename classclaim.yml
```

The whole build process may take a few minutes, typically more than 5 minutes. You can monitor the
progress in TAP GUI, by accessing the Tanzu Developer Portal as instructed in the Developer Sandbox
UI. Then navigate to Supply Chain > sso-client. Once the Delivery box is green, you can click on it
an obtain your Workload URL. Navigate to it, and then log in using usename `user` and password 
`password`.
