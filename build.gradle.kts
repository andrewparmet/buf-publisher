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

fun versionsToPublish(): List<Version> =
    listOf(
        "1.10.0",
        "1.11.0",
        "1.12.0",
        "1.13.0",
        "1.13.1"
    ).flatMap { version ->
        listOf(Os.MAC, Os.WINDOWS, Os.LINUX).flatMap { os ->
            os.architectures.map { arch ->
                Version(version, os, arch)
            }
        }
    }

tasks.register<Download>("downloadBinaries") {
    val urls =
        versionsToPublish().associate {
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
        create<MavenPublication>("main") {
            versionsToPublish().forEach {
                artifact(file("$buildDir/buf/${it.version}/${it.fileName}")) {
                    classifier = "${it.version}-${it.os.classifierName}-${it.arch}"
                    extension = "exe"
                }
            }

            this.artifactId = "buf"
            this.version = project.version.toString()
            this.groupId = "com.parmet.buf"
        }
    }
}

tasks.withType<AbstractPublishToMaven> {
    dependsOn(tasks.named("downloadBinaries"))
}
