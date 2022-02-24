# Roadmap for Solver

Upcoming considerations to improve _Solver_:

## Near term feature goals and issues

- More archetypes for `solver create`, currently just `linux` and `basic` maybe next.
- Enhance help descriptions for each command and parameter.
- Add more unit tests for oddball CLI values.
- `solver lint` is improving:
  - Many checks exist for authoring mode. Considering checks when in challenge context.
  - Encourage authors in documentation to add pre-commit hook to reject commit if `solver lint` returns error code. Will provide a recipe.
  - `solver lint` could offer a `--fix` switch to correct some of the problems it encounters.

The current commands for _Solver_ are marked with their implementation and testing status:

| Working | Command/Switch        | Description |
|---------|-----------------------|-------------|
| ✔       | -h, --help           | Show this help message and exit. |
| ✔       | -V, --version        | Print version information and exit. |
| ✔       | solutions, sol       | Install solutions for testing. Requires authoring passcode. |
| ✔       | next                 | Solve current task and on success advance current task number |    
| ✔       | all                  | Solve all remaining tasks |
| ✔       | until                | Solve all tasks from current task until reaching given task number |
| ✔       | verify               | Verify task number is complete |
| ✔       | hint                 | Get hint given a task number and hint number |
| ✔       | view                 | Reveal the verifications, hints, and solutions for a task |
| ✔       | reset                | Clear task tracker so next task is assumed to be 1 |
| ✔       | status               | Get the next task to solve |
| ✔       | request_hint         | internal call by `hint.sh` only |
| ✔       | request_advance_task | internal call by verify.sh only |
| ✔       | create               | Create a Challenge project from the given archetype when in authoring context |
| ✔       | lint                 | Verify the required artifacts for the challenge are present and valid. Can check-in authoring mode. Checking Challenge mode after decryption is proposed. |

## Longer-term feature goals

- ☢ The CLI binary (Linux native) is currently above the 9MB limit for scenario asset size. The CLI binary has been compressed using UPX, but so far cannot be distilled below 10.6MB. For the time being it is recommended to `wget` the tool from the GitHub release page when the challenge starts. This can potentially lead to the challenge not working if GitHub fails to deliver the artifact due to issues such as GitHub stability, rate limiting, or pure networking.
- Currently assuming all solutions are in a single sh file, instead, consider putting all contents in a `solutions` directory into an encrypted zip.
- Perhaps a verbose logging switch currently logs in `/var/log/solver.log`.
- OSX and Windows native binary on the release page, a container image is currently encouraged
- Add .cypress tests to archetypes. Cypress tests will call `solver all`. Currently, Cypress testing is generally not working for scenarios or challenges. May be helpful to layer in BBD with Cucumber with Cypress.

## Your feedback is important

Your insights as a Challenge author around the authoring process and how to improve this utility and the rest of the platform are important. When you have feedback please consider adding an [issue](https://github.com/javajon/katacoda-solver/issues) to this project. Issues beyond the scope of authoring challenges with _Solver_ and more about Katacoda may be emailed to support@katacoda.com.
