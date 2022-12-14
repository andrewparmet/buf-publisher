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

import io.github.gradlenexus.publishplugin.NexusPublishExtension
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.the
import org.gradle.kotlin.dsl.withType
import org.gradle.plugins.signing.SigningExtension

private object Pgp {
    val key
        get() = System.getenv("PGP_KEY")?.replace('$', '\n')

    val password
        get() = System.getenv("PGP_PASSWORD")
}

private object Remote {
    val username
        get() = System.getenv("OSSRH_USERNAME")

    val password
        get() = System.getenv("OSSRH_PASSWORD")
}

object ProjectInfo {
    const val name = "Buf"
    const val url = "https://github.com/andrewparmet/buf-publisher"
    const val description = "Buf binaries for the buf-gradle-plugin"
}

fun Project.configurePublishing() {
    apply(plugin = "io.github.gradle-nexus.publish-plugin")

    configure<NexusPublishExtension> {
        repositories {
            sonatype {
                username.set(Remote.username)
                password.set(Remote.password)
            }
        }
        packageGroup.set("com.parmet.buf")
    }

    if (isRelease()) {
        apply(plugin = "signing")

        configure<SigningExtension> {
            useInMemoryPgpKeys(Pgp.key, Pgp.password)

            the<PublishingExtension>().publications.withType<MavenPublication> {
                standardPom()
                sign(this)
            }
        }

        afterEvaluate {
            tasks.named("signMainPublication") { dependsOn("downloadBinaries") }
        }
    }
}

private fun MavenPublication.standardPom() {
    pom {
        name.set(ProjectInfo.name)
        description.set(ProjectInfo.description)
        url.set(ProjectInfo.url)
        scm {
            url.set(ProjectInfo.url)
        }
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("Andrew Parmet")
                name.set("Andrew Parmet")
                email.set("andrew@parmet.com")
            }
        }
    }
}

fun Project.isRelease() = !version.toString().endsWith("-SNAPSHOT")
