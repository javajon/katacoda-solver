# Solver Tool for Authoring O'Reilly Challenges

_Solver_ is a command-line interface ([CLI](https://en.wikipedia.org/wiki/Command-line_interface)) that helps authors create [O'Reilly Challenges](https://www.katacoda.community/challenges/challenges.html). _Solver_ helps authors organize the verifications, the myriad of hints, and provides an enhanced solutions mechanism for rapid testing. _Solver_ is not required, but it can shorten your time for producing quality Challenges.

The usage of _Solver_ for O'Reilly Challenge authors is detailed in the [Katacoda Documentation](https://www.katacoda.community/challenges/challenges-solver.html).

The command line offers information on the commands via `solver --help`.

If you are an author using this utility, your feedback is important, and please feel free to add issues in this project for reporting problems or suggesting ideas.

## Running Solver for Authors

As an author there are two places where Solver can help you:

1) Locally in your development environment when creating the content
2) At runtime, within the challenge.

Solver is a tool that both supports your challenge development and execution.

These two installations types are covered in the documentation at [Challenges Solver Utility](https://www.katacoda.community/challenges/challenges-solver.html).

## Solver on Windows and MacOS

The solver CLI utility binary is targeted for Linux. To use Solver while you are developing your challenge on Windows or macOS one option is to run the utility from the published container for solver. Here is a bash function that you can apply if you decide to use a bash terminal on Windows or macOS:

```bash
function solver() {
  SOLVER_IMAGE=ghcr.io/javajon/solver:0.5.4   ## <-- Set to the latest semver release @ https://bit.ly/3sSEiBD
  SCENARIOS_ROOT=~/my-scenarios               ## <-- Set to your base source directory for your challenges and scenarios
  if [[ ! "$(pwd)" =~ ^$SCENARIOS_ROOT.* ]]; then
    echo "Please run this from $SCENARIOS_ROOT or one of its scenario or challenge subdirectory."
    return 1;
  fi
  SUBDIR=$(echo $(pwd) | grep -oP "^$SCENARIOS_ROOT\K.*")    ## <-- change to ggrep if on macOS
  docker run --rm -it -v "$SCENARIOS_ROOT":/workdir -w /workdir/$SUBDIR $SOLVER_IMAGE "$@";
}
```

### macOS Nuance

The above function expects GNU grep. This is not the version installed by default on macOS. You can easily install the GNU version by using [homebrew](https://brew.sh/) and then just changing the call to `grep` in the above function to `ggrep`.

### Windows Nuance

On Windows, another option is [wsl2](https://docs.microsoft.com/en-us/windows/wsl/about).

---

The remaining instructions are for developers of this utility, not for authors of the challenges.

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

If the `quarkusBuild` fails with an exit code 137 out of memory message, the error message suggests increasing the xmx heap size. Others indicate this is a problem with your local docker engine. Consider a [purge](https://www.digitalocean.com/community/tutorials/how-to-remove-docker-images-containers-and-volumes) of your docker engine caches `docker purge system --all` followed with a full host reboot.

With either of the above, you can then execute your native executable with:

```shell script
./build/solver-[version]-runner
```
This binary, renamed to `solver`, may be used by a Challenge.


## Developing and Debugging the Solver project code

If you are a developer or tester of the _Solver_ utility below are some techniques to run it locally or copy it to a Challenge without the GitOps process.

You can run `solver` from Linux shells, but without the context of an O'Reilly Challenge, it is like watching Fred Flintstone drive a car with his feet with no engine. You're not fooling anyone, Fred.

### Run Locally via _über-jar_

```shell script
./gradlew build -Dquarkus.package.type=uber-jar && java -jar build/quarkus-app/quarkus-run.jar --help
```

### Installing Solver into a Challenge

The `solver` command-line tool cannot be loaded via the scenario's assets as there is a size limit at 9MB and the CLI tool is too large of an asset. Instead, there is a `wget` command in the `init-background.sh` script that installs _Solver_ when the challenge starts. This incurs a slight vulnerability if GitHub fails to deliver the requested CLI binary artifact from the release page then the challenge will break and the learner will have to reload the scenario. This source may change and remains on the roadmap.

Ensure the `wget` pulls a specific version of _Solver_ and your challenge is tested with that specific version in place.

### Rapid Development Testing

For fast, local, iterative development and testing of the _Solver_ tool with a live challenge it's best to copy the updated _Solver_ binary directly to the challenge. There are a variety of places where a binary can be uploaded. Here is an example using the public service [transfer.sh](https://transfer.sh/)

1. Build the binary with
   ```shell script
   ./gradlew build -Dquarkus.package.type=native -Dquarkus.native.container-build=true
   ```
2. Identify the binary to copy:
   ```shell script
   export SOLVER_RUNNER=build/solver-$(git describe --tags $(git rev-list --tags --max-count=1))-$(git rev-parse --abbrev-ref HEAD)-runner

3. Upload the executable to an ephemeral cloud directory:
   ```shell script
   STORAGE_URL=$(curl --upload-file $SOLVER_RUNNER https://transfer.sh/solver) && echo $STORAGE_URL
   ```
4. Copy the resulting storage URL to your clipboard.
5. Start Challenge
6. Copy `solver` binary into Challenge using the clipboard pasted URL:
   ```shell script
   curl -o solver <pasted-url>
   ```
7. Make executable, copy, and verify:
   ```shell script
   chmod +x solver && cp solver /usr/local/bin && solver --version
   ```

Some of these public services throttle throughput over repeated usage. You can also use [gdrive](https://github.com/prasmussen/gdrive) to download artifacts from Google drive. Never use these for the published challenges.

To compress the binary before transfer use [UPX](https://upx.github.io/). The releases are compressed with this UPX tool:
- Install UPX with
  ```
  sudo apt-get update && yes | sudo apt-get install upx
  ```
- Compress the executable with:
  ```
  upx --best --lzma $solver
  ````

## Solver Version Tracking

_Solver_ uses SemVer and the versions are tracked and bumped automatically. A release is created for any commit with a new SemVer git tag. There are GitHub actions to build, tag, and create releases. The SemVer tagging, bumping, and releasing process is based on the GitHub action [jefflinse/pr-semver-bump](https://github.com/jefflinse/pr-semver-bump).

Direct commits are not permitted to the main branch via a GitHub branch rule. Only PRs are committed to main. A Merged pull request (PR) triggers the automated SemVer advancement and a new [release](https://github.com/javajon/katacoda-solver/releases). With this comes the PR comments and PR labels and direct the bumping of the major, minor, and patch numbers. When a PR is merged to `main`, it **must** be labeled with either `major`, `minor`, or `patch`. When a new SemVer tag is created a new GitHub release is created with the update `solver` binary. This technique follows some best practices for automated GitOps. Branch names can be reused, such as `update`. The workflow for the PR roughly follows this flow:

```shell script
git checkout -b update
(make changes)
git add .
git commit -m "(the reasons for the new release)"
git push --set-upstream origin update
```

In GitHub merge the pull request and be sure to add the label **patch release**, as described [here](https://github.com/jefflinse/pr-semver-bump#inputs).

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
