import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.5.0-RC2"
    id("application")
    id("idea")
    id("io.github.ben-manes.versions") version "0.67.0" // dependencyUpdates task
}

kotlin {
    jvmToolchain(27)
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.compilerOptions {
    freeCompilerArgs.add("-opt-in=kotlin.contracts.ExperimentalContracts")
    freeCompilerArgs.add("-opt-in=kotlin.io.path.ExperimentalPathApi")
    freeCompilerArgs.add("-opt-in=kotlinx.coroutines.DelicateCoroutinesApi")
    freeCompilerArgs.add("-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi")
}

sourceSets {
    main {
        kotlin.srcDir("src")
    }
    test {
        kotlin.srcDir("test")
    }
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")
    implementation("org.jgrapht:jgrapht-core:1.5.3")
    testImplementation("org.junit.jupiter:junit-jupiter:6.1.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation("io.arrow-kt:arrow-core:2.3.0.1")
    implementation("io.arrow-kt:arrow-fx-coroutines:2.3.0.1")
}

application {
    mainClass.set(
        if (hasProperty("allDays")) {
            "AllKt"
        } else if (hasProperty("year") && hasProperty("day")) {
            "year${property("year")}.Day${property("day")}Kt"
        } else {
            "-Pyear=YYYY and -Pday=DD should be set"
        }
    )
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

idea {
  module {
    excludeDirs.add(file("inputs"))
  }
}