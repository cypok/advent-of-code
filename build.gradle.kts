plugins {
    kotlin("jvm") version "2.1.0"
    id("application")
    id("idea")
}

kotlin {
    jvmToolchain(23)
}

sourceSets {
    main {
        kotlin.srcDir("src")
    }
    test {
        kotlin.srcDir("test")
    }
}

tasks {
    wrapper {
        gradleVersion = "8.11.1"
    }
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("org.jgrapht:jgrapht-core:1.5.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")
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