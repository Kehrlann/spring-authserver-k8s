# build image using gradle
custom_build(
    "springone/authserver",
    command = "./gradlew bootBuildImage --imageName $EXPECTED_REF",
    deps=["src/main", "build.gradle.kts"]
)

# run the deployment
k8s_yaml(["infrastructure.yml", "deployment.yml"])

# "Attach" the ingress and namespace to the "authserver" resource
# This avoids having an "uncategorized" resource in tilt
k8s_resource("authserver", objects=["authserver:Ingress:authserver", "authserver:Namespace"])
