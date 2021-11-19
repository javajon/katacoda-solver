# Challenge Scenario Authoring and Testing Instructions

## Critical Scenario and Challenge Files

The author is responsible for creating and maintaining these files:

| Source file            | Purpose |
| ---------------------- | ----------- |
| index.json             | Core definition of any scenario (challenge or not). |
| challenge.sh           | The presentation of task instructions to the learner. Must be in the scenario folder's `/assets/` subfolder and copied to /usr/local/bin. This is the place for bash scripts using the Challenges API, and is where you define all of the tasks, text, tests, and (optionally) hints. |
| solutions.sh      | The commands that provide the required instructions to complete each task. Solutions are organized into bash functions. |
| verifications.sh  | The commands that verify each task has been completed. Verifications are organized into bash functions. |
| solver                 | Internal utility for developers and testers to solve and validate tasks. See `solver help`. Also called by challenge.sh |
| background.sh          | Configure the challenge environment here. Install anything, preload data, break things for the learner to fix later, go nuts! |
| foreground.sh          | Verifies all the configuration in `background.sh` is complete, then calls the challenge script. |

## Testing the Challenge Scenario with the Solutions

There is a `/usr/local/bin/solver` script. Its purpose is for internal validation of each scenario task. It is not shared with the learners. The scenario author must add solutions and verifications for each task.

Run `solver` to see the usage instructions.

## index.json Contents

Some details about the index.json contents:

- the essential `"type": "challenge@[version]"` (check for the latest version),
- the `intro` property calls both `background.sh` and `foreground.sh` immediately on load,
- there are no "steps" or "finish" markdown instructions defined (unlike a regular scenario, since challenges do not include any lesson text),
- the `assets` property specifies the files above that are be copied to the environment's `/usr/local/bin/` and made executable,
- `"hidesidebar": true` because there is no lesson text,
- `imageid` Use whatever [base image](https://www.katacoda.community/environments.html) is most appropriate for your challenge.

## Tips

Validate your scripts with https://www.shellcheck.net/.

Correct wording in intro.md, challenge.sh, and any other text content through Grammerly.

## Testing this Scenario with Solver Utility

For testing this scenario with the solver utility, copy the `solutions.sh` shell script file to the scenario's directory `/usr/local/bin/`. The solution file can be found in the [scenario root source directory.](https://resources.oreilly.com/katacoda/jonathan-johnson/raw/master/challenge-hints-sandbox/assets/solutions.sh)

Once copied, the actions like `solver next` and `solver all` enable the solver to complete the steps for you.
