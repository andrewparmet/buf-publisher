/*
 * Copyright (c) 2022 Andrew Parmet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import de.undercouch.gradle.tasks.download.Download

repositories {
    gradlePluginPortal()
}

version = "1.0.0"

plugins {
    `maven-publish`
    alias(libs.plugins.download)
    alias(libs.plugins.publishing)
    alias(libs.plugins.spotless)
}

configurePublishing()

allprojects {
    spotless {
        kotlinGradle {
            ktlint()
        }
    }
}

enum class Os(
    val bufName: String,
    val classifierName: String,
    val architectures: List<String>
) {
    MAC("Darwin", "osx", listOf("arm64", "x86_64")),
    WINDOWS("Windows", "windows", listOf("arm64", "x86_64")),
    LINUX("Linux", "linux", listOf("aarch64", "x86_64"))
}

data class Version(
    val version: String,
    val os: Os,
    val arch: String
) {
    val fileName = "buf-${os.bufName}-${arch + if (os == Os.WINDOWS) ".exe" else ""}"
}

fun versions(): List<Version> =
    listOf("1.0.0", "1.1.0", "1.8.0").flatMap { version ->
        listOf(Os.MAC, Os.WINDOWS, Os.LINUX).flatMap { os ->
            os.architectures.map { arch ->
                Version(version, os, arch)
            }
        }
    }

tasks.register<Download>("downloadBinaries") {
    val urls =
        versions().associate {
            "https://github.com/bufbuild/buf/releases/download/v${it.version}/${it.fileName}" to it.version
        }

    src(urls.keys)

    eachFile {
        name = "${urls[sourceURL.toString()]}/$name"
    }

    onlyIfModified(true)

    dest("$buildDir/buf")
}

publishing {
    publications {
        versions().forEach {
            val existing = findByName("main-${it.version}") as MavenPublication?
            with(existing ?: create<MavenPublication>("main-${it.version}")) {
                artifact(file("$buildDir/buf/${it.version}/${it.fileName}")) {
                    classifier = "${it.os.classifierName}-${it.arch}"
                    extension = "exe"
                }

                this.artifactId = "buf"
                this.version = it.version
                this.groupId = "com.parmet.buf"
            }
        }
    }
}

tasks.withType<AbstractPublishToMaven> {
    dependsOn(tasks.named("downloadBinaries"))
}