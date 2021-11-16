# Roadmap for Solver

This is a list of upcoming considerations to improve the tool:

## Near term feature goals and issues

- Update documentation page on challenges with solver
- Release notes on public repo replace page are blank
- After archetype project is created, copy latest solver release from public repo release page
- Prevent commands from running in the wrong context
- Add more unit testing

The current commands for Solver are marked with their implementation and testing status:

| Status  | Command/Switch        | Description |
|---------|-----------------------|-------------|
| ✔       | -h, --help           | Show this help message and exit. |
| ✔       | -V, --version        | Print version information and exit. |
| ✔       | solutions, sol       | Install solutions for testing. Requires authoring passcode placed in challenge source repo. |
| ✔       | next                 | Solve current task and on success advance current task number |    
| 🤔 todo  | all                  | Solve all remaining tasks |
| 🤔 todo  | until                | Solve all remaining tasks until reaching given task number |
| ✔       | solve                | Solve task number. Subsequent commands assume next task |
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

- Create archetype for linux works, but could create a few more. A blank/scratch archetype would be helpful.
- Add .cypress tests to archetypes
- Currently assuming all solutions are in a sh file, instead put all contents in solutions directory into an encrpyted zip.
- Perhaps a verbose logging switch, currently logs in /var/log/solver.log

## Feedback is Important

Your insights as a Challenge author around the authoring process and how to improve this utility and the rest of the platform is important. When you have feedback please consider adding an issue to this project or email me directly jonathan.johnson@dijure.com. Issues outside of the solver utility and more about the platform may be submitted to support@katacoda.com.
