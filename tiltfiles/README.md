# Tiltfiles

Showcase sample tiltfiles for building a Boot image with tilt, including different setups for x86
and ARM architecutres.

## buildpacks-liveupdate

Uses buildpacks for the initial build, and then uses Tilt's live-update feature for subsequent
builds.

Has a long-running gradle "continuous build" process running in the background, and watches the
output of the build directory to do a live-update.

This is slow on arm-based Macs (m1/m2), because the buildpacks produce an x86-based image. That
image is then very slow to start (10 times slower than an arm native image). One workaround would be
to use @dashaun's builder image, however that builder does not produce live-update compatible
images, as it's a `tiny` image and it is missing the `tar` binary:

```kotlin
tasks.bootBuildImage {
	environment.put("BP_LIVE_RELOAD_ENABLED", "true")
	builder.set("dashaun/builder:tiny")
}
```

Pros:
- Really fast on x86 machines

Cons:
- Slightly hackier as we don't watch source files, but rather depend on Gradle's classpath changes
- Slow on arm-based Macs, because restarting the app takes 15-20s

## buildpacks-redeploy

Uses buildpacks. Tilt watches for file changes in `src/main`, and re-builds and re-deploys the image
to the cluster everytime a source file changes. This is much slower than the `liveupdate` version,
as rebuilding with buildpacks takes approximately 20s. However, rolling the deployment over in the
cluster is fast.

Don't forget to user `dashaun/build:tiny` as the builder image.

Pros:
- Tiltfile is straightforward

Cons:
- Slower than live-update on x86 machines


