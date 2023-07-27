plugins {
	java
	id("org.springframework.boot") version "3.1.0"
	id("io.spring.dependency-management") version "1.1.0"
}

group = "wf.garnier.springone"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
	implementation("org.springframework.boot:spring-boot-starter-oauth2-authorization-server")
	implementation("io.kubernetes:client-java:18.0.0")
	implementation("io.kubernetes:client-java-extended:18.0.0")
	implementation("org.webjars:webjars-locator-core")
	implementation("org.webjars:bootstrap:5.2.3")
	implementation("org.webjars:popper.js:2.9.3")
	implementation("org.webjars:jquery:3.6.4")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.bootBuildImage {
	environment.put("BP_LIVE_RELOAD_ENABLED", "true")
	builder.set("dashaun/builder:tiny")
}