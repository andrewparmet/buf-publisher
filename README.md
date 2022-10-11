# buf-publisher

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