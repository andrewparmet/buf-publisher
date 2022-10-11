# buf-publisher

[![Maven Central](https://img.shields.io/badge/dynamic/xml?color=orange&label=maven-central&prefix=v&query=%2F%2Fmetadata%2Fversioning%2Flatest&url=https%3A%2F%2Frepo1.maven.org%2Fmaven2%2Fcom%2Fparmet%2Fbuf%2Fbuf%2Fmaven-metadata.xml)](https://search.maven.org/artifact/com.parmet.buf/buf)

Publishes Buf binaries to Maven Central for consumption by JVM projects. Written to support the [Buf Gradle Plugin](https://github.com/andrewparmet/buf-gradle-plugin).

## Table of Contents

<!--ts-->
* [buf-publisher](#buf-publisher)
   * [Table of Contents](#table-of-contents)
      * [Publishing](#publishing)

<!-- Created by https://github.com/ekalinin/github-markdown-toc -->
<!-- Added by: andrewparmet, at: Mon Oct 10 19:55:20 EDT 2022 -->

<!--te-->
<!-- Regenerate with `./gh-md-toc --insert README.md` -->

### Publishing

In contrast to other published binaries like `protoc`, which are published and tagged according to the binary itself, this project's versioning scheme is to publish each configured version of Buf to an artifact versioned according to this project. If a specific version has an issue then all versions of Buf can be republished.

CI is configured through GitHub Actions. For manual publication:

Run to publish to Sonatype's staging repository:
```
OSSRH_USERNAME=<username> \
OSSRH_PASSWORD=<password> \
PGP_KEY=<key-with-$-instead-of-newlines> \
PGP_PASSWORD=<passphrase> \
./gradlew \
publishToSonatype \
-Dorg.gradle.internal.http.socketTimeout=120000 \
-Dorg.gradle.internal.network.retry.max.attempts=1 \
-Dorg.gradle.internal.publish.checksums.insecure=true
```

Then close the repository and test locally.

Then promote the repository.
