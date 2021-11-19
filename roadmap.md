# Roadmap for Solver

Upcoming considerations to improve the tool:

## Near term feature goals and issues

- Update documentation page on challenges with solver
- Release notes on the public repo release page are blank
- OSX and Windows native binary on release page
- Archetype project created needs version of solver installed
- Test command line for odd ball values, eg. solver all when solutions not decrypted.
- Add more unit testing
- Enhance help descriptions for each command
- Add .cypress tests to archetypes

The current commands for Solver are marked with their implementation and testing status:

| Status  | Command/Switch        | Description |
|---------|-----------------------|-------------|
| ✔       | -h, --help           | Show this help message and exit. |
| ✔       | -V, --version        | Print version information and exit. |
| ✔       | solutions, sol       | Install solutions for testing. Requires authoring passcode placed in challenge source repo. |
| ✔       | next                 | Solve current task and on success advance current task number |    
| ✔       | all                  | Solve all remaining tasks |
| 🤔 todo  | until                | Solve all tasks from current task until reaching given task number |
| ✔       | verify               | Verify task number is complete |
| ✔       | hint                 | Peak at hint ID for the task number. Omitting # assumes current task |
| ✔       | view                 | Reveal the verifications, hints, and solutions for a task. |
| ✔       | reset                | Clear task tracker so next task is assumed to be 1 |
| ✔       | status               | Get the current step and hint. |
| ✔       | request_hint         | internal call |
| ✔       | request_advance_task | internal call |
| ✔       | create               | Create any missing files that are needed by Solver. Will not overwrite. Must be authoring. |
| 🤔 todo  | check                | Verify the required artifacts for the challenge are present and valid. Can check authoring and challenge environments. |

## Longer term feature goals


- ☢ The CLI binary (linux native) is currently above the 9MB limit for scenario asset size. The CLI binary has been compressed using UPX, but so far cannot be distilled below 9MB. For the time being it is recommended to wget the binary from the GitHub release page when the challnege starts. This can potentially lead to the challenge not working if GitHub failes to deliver the artifact due to issues such as GitHub stability, rate limiting, or pure networking.
- Create archetype for linux works, but could create a few more. A blank/scratch archetype would be helpful.
- Currently assuming all solutions are in a sh file, instead put all contents in solutions directory into an encrpyted zip.
- Perhaps a verbose logging switch, currently logs in /var/log/solver.log
- Consider optional container image on release page.

## Feedback is Important

Your insights as a Challenge author around the authoring process and how to improve this utility and the rest of the platform is important. When you have feedback please consider adding an [issue](https://github.com/javajon/katacoda-solver/issues) to this project or email me directly jonathan.johnson@dijure.com. Issues outside of the solver utility and more about the platform may be submitted to support@katacoda.com.
