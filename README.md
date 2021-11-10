# Solver Tool for O'Reilly Challenges

Solver is a command-line interface ([CLI](https://en.wikipedia.org/wiki/Command-line_interface)) that helps authors create [O'Reilly Challenges](https://www.katacoda.community/challenges/challenges.html). The Solver utility helps authors organize the verifications, the myriad of hints, and provides an enhanced solutions mechanism for rapid testing. Solver is not required, but it can shorten your time for producing quality Challenges.

The usage of the Solver utility for O'Reilly Challenge authors is detailed in the [Katacoda Documentation](https://www.katacoda.community/challenges/challenges-solver.html).

The command line offers information on the commands via `solver --help`.

If you are an author using this utility, your feedback is important and please feel free to add issues in this project for reporting problems or suggesting ideas.

## Installing Solver into a Challenge

The recommended way to install Solver into a Challenge is to copy a specific release binary from the solver releases list into the `assets` directory of the source for a Challenge. This way each Challenge has the solver utility installed when it starts and it's always the version that was tested with the scenario. Copy the binary into the Challenge `assets` directory:

```sh
VERSION=0.9.1
TARGET=assets/solver
wget -O $TARGET https://github.com/javajon/katacoda-solver/releases/download/$VERSION/solver-$VERSION-runner
chmod +x $TARGET
```
Change the `VERSION=` setting to the latest (or desired) SemVer listed on the [releases page](https://github.com/javajon/katacoda-solver/releases). Add this copy instruction in the assets section of `index.json`:

```yaml
"assets": {
  "host01": [
    {"file": "solver", "target": "/usr/local/bin/", "chmod": "+x"}
  ]
} 
```
If you are running and testing with a live Challenge and want to test with a different or updated version of solver then install an alternate version that specifies the target.

☢ Please avoid attempting to download the solver binary from GitHub on the scenario launch. There are rate limits and reliability issues related to GitHub that can cause your scenario to break for learners.

### Rapid Development Testing

For fast, local, iterative development and testing of the solver tool with a live challenge its best to copy the updated solver binary directly to the challenge. There are a variety of places where a binary can be uploaded. Here is an example using the public service [transfer.sh](https://transfer.sh/):

1. Build the binary with `./gradlew build -Dquarkus.package.type=native -Dquarkus.native.container-build=true`
2. Upload: STORAGE_URL=$(curl --upload-file build/solver-0.1.1-SNAPSHOT-runner https://transfer.sh/solver) && echo $STORAGE_URL
3. Start Challenge
4. Copy solver binary into Challenge: curl -o solver https://transfer.sh/[id]]/solver
5. Make executable, copy, and verify: chmod +x solver; cp solver /usr/local/bin; solver --version

## Architecture stack

This command-line tool is written in Java. [Picocli](https://picocli.info/) is leveraged for the CLI framework. [Quarkus](https://quarkus.io/) with Graal creates a fast and efficient Linux binary. The project is built with Gradle and CI/CD is automated using GitHub actions.

## Running App in Dev Mode

You can run your application in developer mode that enables live coding using:

```shell script
./gradlew quarkusDev
```

## Packaging and Running

The application can be packaged using:

```shell script
./gradlew build
```

It produces the `quarkus-run.jar` file in the `build/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `build/quarkus-app/lib/` directory.

If you want to build an _über-jar_, execute the following command:

```shell script
./gradlew build -Dquarkus.package.type=uber-jar
```

The application is now runnable using `java -jar build/quarkus-app/quarkus-run.jar`.

## Creating Native Executable

You can create a native executable using:

```shell script
./gradlew build -Dquarkus.package.type=native -Dquarkus.native.native-image-xmx=8g
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using:

```shell script
./gradlew build -Dquarkus.package.type=native -Dquarkus.native.container-build=true -Dquarkus.native.native-image-xmx=8g
```

If the quarkusBuild fails with an exit code 137 out of memory message, the error message suggests increasing the xmx heap size. Others indicate this is a problem with your local docker engine. Consider a [purge](https://www.digitalocean.com/community/tutorials/how-to-remove-docker-images-containers-and-volumes) of your docker engine caches `docker purge system --all` followed with a full host reboot.

With either of the above, you can then execute your native executable with:

```shell script
./build/solver-[version]-runner
```
This binary, rename to `solver`, may be used by a Challenge.

## Running Solver

As an author, the recommended way to install Solver into your O'Reilly Challenge is covered in the documentation at [Challenges Solver Utility(https://www.katacoda.community/challenges/challenges-solver.html).

If you are a developer or tester of the Solver utility below are some techniques to run it locally or copy it to a Challenge without the GitOps process.

You can run Solver from Linux shells, but without the context of an O'Reilly Challenge, it is like watching Fred Flintstone drive a car with his feet with no engine. You're not fooling anyone, Fred.

### Run Locally via _über-jar_

```shell
./gradlew build -Dquarkus.package.type=uber-jar && java -jar build/quarkus-app/quarkus-run.jar --help
```
## Solver Version Tracking

The Solver uses SemVer and the versions are tracked automatically. A release is created for any commit with a new SemVer git tag. There are GitHub actions to build, tag, and create releases. The SemVer tagging, bumping, and releasing process is based on the GitHub action [jefflinse/pr-semver-bump](https://github.com/jefflinse/pr-semver-bump). 

A Merged pull requests (PRs) triggers the automated SemVer advancement. With this comes the PR comments and PR labels and direct the bumping of the major, minor, and patch numbers. When a new SemVer tag is created a new GitHub release is created with the updated Solver binary. This technique follows some best practices for automated GitOps.

## Related Guides

- [O'Reilly Challenges](https://www.katacoda.community/challenges/challenges.html) and a [Challenge Examples](https://katacoda.com/scenario-examples/courses/challenges).

- [Picocli](https://picocli.info/)

- [Quarkus](https://quarkus.io/)

- To learn more about building native executables, see [Building Quarkus Apps with Gradle](https://quarkus.io/guides/gradle-tooling).

- This Java application uses Picocli ([guide](https://quarkus.io/guides/picocli)) as a very helpful framework for developing intuitive and consistent command-line applications.

- More about [Command line application with multiple Commands](https://quarkus.io/guides/picocli#command-line-application-with-multiple-commands).

- An excellent alternative to Quarkus is Micronaut that is a similar build process since it's also based on Graal. [Micronaut also works well with Picocli](https://picocli.info/#_micronaut_example).

## Origins

This project was inspired by the [try-picocli-gradle](https://github.com/ia3andy/try-picocli-gradle) repository.

