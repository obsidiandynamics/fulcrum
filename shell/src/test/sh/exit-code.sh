#!/bin/sh
#
# Used for testing exit codes.
#

if [ $# != 1 ]; then
  echo "Usage exit-code.sh <exit code>"
  exit 1
fi

exit $1