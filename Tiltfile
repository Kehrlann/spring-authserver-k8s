# Deploy the `default-key.yml` secret ; if it is managed by Tilt
# it tends to be reapplied every time a workload updates, so we can't
# really change it's `security.spring.io/key-usage` label to verification
# and have it persist
local("kubectl apply -f default-key.yml")

# Build the auth server image using gradle
custom_build(
    "springone/authserver",
    command = "./gradlew bootBuildImage --imageName $EXPECTED_REF",
    deps=["src/main", "build.gradle.kts"]
)

# Deploy the auth-server
k8s_yaml(["rbac.yml", "deployment.yml"])

# "Attach" the ingress and namespace to the "authserver" resource
# This avoids having an "uncategorized" resource in Tilt UI
k8s_resource(
    "authserver",
    objects=[
        "authserver:ingress",
        "authserver:serviceaccount",
        "secret-reader:role",
        "read-secrets:rolebinding",
    ]
)

# Deploy the token-viewr
k8s_yaml("token-viewer.yml")

k8s_resource(
    "token-viewer",
    objects=["token-viewer:ingress"]
)
