import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "2.7.8"
	id("io.spring.dependency-management") version "1.1.0"
	kotlin("jvm") version "1.8.10"
	kotlin("plugin.spring") version "1.8.10"
	groovy
}

group = "pt.isel"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
	mavenCentral()
	mavenLocal()
}

sourceSets {
	main {
		java {
			setSrcDirs(emptyList<String>())    // no source dirs for the java compiler
		}
		groovy {
			setSrcDirs(listOf("src/main/java", "src/main/groovy"))  // compile   everything in src/ with groovy
		}
	}
}

dependencies {
	implementation("org.slf4j:slf4j-simple:2.0.6")
	/**
 	 * DSLs for HTML
	 */
	implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.8.0")
	implementation("com.github.xmlet:htmlflow:4.4-SNAPSHOT")
	implementation("io.reactivex.rxjava3:rxjava:3.1.8")
	implementation("com.googlecode.jatl:jatl:0.2.3")
	implementation("com.j2html:j2html:1.6.0")
	/**
	 * Spring infrastructure
	 */
	implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
	/**
	 * Kotlin  infrastructure
	 */
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
	/**
	 * Groovy
	 */
	implementation("org.apache.groovy:groovy-all:4.0.14")
	/**
	 * Test
	 */
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("io.projectreactor:reactor-test")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "11"
	}
}

tasks
	.getByName<AbstractCompile>("compileGroovy")
	.dependsOn(tasks.getByName<KotlinCompile>("compileKotlin"))

tasks
	.getByName<AbstractCompile>("compileGroovy")
	.classpath += files(tasks.getByName<KotlinCompile>("compileKotlin").destinationDirectory)

tasks.withType<Test> {
	useJUnitPlatform()
}

