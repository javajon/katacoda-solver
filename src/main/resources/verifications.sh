#!/bin/bash

function verify_task_1() {
   echo "Task 1 verification"
   if [ ! -f 'test.txt' ]; then
      return 1
   fi
}

function verify_task_2() {
   echo "Task 2 verification"
}

function verify_task_3() {
   echo "Task 3 verification"
   return 0
}
