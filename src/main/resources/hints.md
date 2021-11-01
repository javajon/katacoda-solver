# Task Hints Authoring Guidelines

When authoring this collection of hints, mark each hint with the double pound (##) markdown header and title the header with Task n, Hint n. Capital T and H. Tasks and Hints start at 1. The delay period before hints are offered is defined and exported as `HINTS_DELAY` at the top of the `verifications.sh` script. See this [Challenge Hints](https://www.katacoda.community/challenges.html#ui-example) in the authoring guide on how to provide hints for your challenge scenarios. TODO - The [Katacoda markdown extensions](https://www.katacoda.community/scenario-syntax.html#katacoda-s-markdown-extensions) can be applied in this markdown.

## Task 1, Hint 1

A Deployment called `redis` has not been rolled out yet to the default namespace. Verify the Pod has started normally with `kubectl get pods`{{execute}} and check its log with `kubectl logs [pod name]`.

## Task 1, Hint 2

The Deployment `redis` was rolled out, but with the wrong number of `replicas`. Just one is needed.

## Task 1, Hint 3

The Deployment `redis` was rolled out, but you specified the wrong image container name and version.

## Task 1, Hint 4

The Deployment `redis` was rolled out, but it missing the expected label. See `kubectl label deployment --help`{{execute}}

## Task 2, Hint 1

A service with the name `redis` was not found in the default namespace. Use the `kubectl expose --help`{{execute}} command to expose the `redis` Deployment.

## Task 2, Hint 2

A service named `redis` was found, but the type is not `ClusterIP`. Delete the service and try again. See `kubectl expose --help`{{execute}}.

## Task 2, Hint 3

A service named `redis` was found, with the type `ClusterIP`, but the service port is not the right value. Delete the service and try again. See `kubectl expose --help`{{execute}}.

## Task 2, Hint 4

Redis service is connected to the `redis` application, but Redis is not yet reporting it's ready to accept traffic.

## Task 3, Hint 1

A Deployment called `voting` has not been rolled out yet to the default namespace. Verify the Pod has started normally with `kubectl get pods`{{execute}} and check its log with `kubectl logs [pod name]`.

## Task 3, Hint 2

The Deployment `voting` was rolled out, but with the wrong number of `replicas`. Just one is needed.

## Task 3, Hint 3

The Deployment `voting` was rolled out, but you specified the wrong image container name and version.

## Task 3, Hint 4

The Deployment `voting` was rolled out, but it missing the expected label. See `kubectl label deployment --help`{{execute}}

## Task 4, Hint 1

A service with the name `voting` was not found in the default namespace. Use the `kubectl expose --help`{{execute}} command to expose the `voting` Deployment.

## Task 4, Hint 2

A service named `voting` was found, but the type is not `NodePort`. Delete the service and try again. See `kubectl expose --help`{{execute}}.

## Task 4, Hint 3

A service named `voting` was found, with the type `NodePort`, but the service port is not the right value. Delete the service and try again. See `kubectl expose --help`{{execute}}.

## Task 4, Hint 4

A service named `voting` was found, with the type `NodePort`, but the nodePort is not the right value. Delete the service and try again. See `kubectl expose --help`{{execute}}.

## Task 5, Hint 1

A Deployment called `db` has not been rolled out yet to the default namespace. Verify the Pod has started normally with `kubectl get pods`{{execute}} and check its log with `kubectl logs [pod name]`.

## Task 5, Hint 2

The Deployment `db` was rolled out, but with the wrong number of `replicas`. Just one is needed.

## Task 5, Hint 3z

The Deployment `db` was rolled out, but you specified the wrong image container name and version.

## Task 5, Hint 4

The Deployment `db` was rolled out, but its missing the expected label. See `kubectl label deployment --help`{{execute}}

## Task 5, Hint 5

The Deployment `db` was rolled out, but its missing the expected environment variable setting `POSTGRES_DB=db`. See `kubectl set env --help`{{execute}}

## Task 5, Hint 6

The Deployment `db` was rolled out, but its missing the expected environment variable setting `POSTGRES_USER=postgres`. See `kubectl set env --help`{{execute}}

## Task 5, Hint 7

The Deployment `db` was rolled out, but its missing the expected environment variable setting `POSTGRES_PASSWORD=postgres`. See `kubectl set env --help`{{execute}}

## Task 5, Hint 8

The Deployment `db` was rolled out, but its missing the expected environment variable setting `POSTGRES_HOST_AUTH_METHOD=trust`. See `kubectl set env --help`{{execute}}

## Task 6, Hint 1

A service with the name `db` was not found in the default namespace. Use the `kubectl expose --help`{{execute}} command to expose the `db` Deployment.

## Task 6, Hint 2

A service named `db` was found, but the type is not `ClusterIP`. Delete the service and try again. See `kubectl expose --help`{{execute}}.

## Task 6, Hint 3

A service named `db` was found, with the type `ClusterIP`, but the service port is not the right value. Delete the service and try again. See `kubectl expose --help`{{execute}}.

## Task 7, Hint 1

A Deployment called `db` has not been rolled out yet to the default namespace. Verify the Pod has started normally with `kubectl get pods`{{execute}} and check its log with `kubectl logs [pod name]`.

## Task 7, Hint 2

The Deployment `result` was rolled out, but with the wrong number of `replicas`. Just one is needed.

## Task 7, Hint 3

The Deployment `result` was rolled out, but you specified the wrong image container name and version.

## Task 7, Hint 4

The Deployment `result` was rolled out, but it missing the expected label. See `kubectl label deployment --help`{{execute}}

## Task 8, Hint 1

A service with the name `result` was not found in the default namespace. Use the `kubectl expose --help`{{execute}} command to expose the `result` Deployment.

## Task 8, Hint 2

A service named `result` was found, but the type is not `NodePort`. Delete the service and try again. See `kubectl expose --help`{{execute}}.

## Task 8, Hint 3

A service named `result` was found, with the type `NodePort`, but the service port is not the right value. Delete the service and try again. See `kubectl expose --help`{{execute}}.

## Task 8, Hint 4

A service named `result` was found, with the type `NodePort`, but the nodePort is not the right value. Delete the service and try again. See `kubectl expose --help`{{execute}}.

## Task 9, Hint 1

A Deployment called `worker` has not been rolled out yet to the default namespace. Verify the Pod has started normally with `kubectl get pods`{{execute}} and check its log with `kubectl logs [pod name]`.

## Task 9, Hint 2

The Deployment `worker` was rolled out, but with the wrong number of `replicas`. Just one is needed.

## Task 9, Hint 3

The Deployment `worker` was rolled out, but you specified the wrong image container name and version.

## Task 9, Hint 4

The Deployment `worker` was rolled out, but it missing the expected label. See `kubectl label deployment --help`{{execute}}
