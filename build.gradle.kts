plugins {
    kotlin("jvm") version "2.5.0-RC2"
    id("application")
    id("idea")
    id("io.github.ben-manes.versions") version "0.67.0" // dependencyUpdates task
}

kotlin {
    jvmToolchain(27)
    compilerOptions {
        optIn.add("kotlin.contracts.ExperimentalContracts")
        optIn.add("kotlin.io.path.ExperimentalPathApi")
        optIn.add("kotlinx.coroutines.DelicateCoroutinesApi")
        optIn.add("kotlinx.coroutines.ExperimentalCoroutinesApi")
    }
}

sourceSets {
    main {
        kotlin.srcDir("src")
    }
    test {
        kotlin.srcDir("test")
    }
}

repositories {
    mavenCentral()
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