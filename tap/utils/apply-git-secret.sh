#!/usr/bin/env bash

# TODO: delete when git auth not required anymore

set -euo pipefail

GIT_TOKEN="$1"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

ytt --file "$SCRIPT_DIR/../git-secret.yml" \
  --data-value "token=$GIT_TOKEN" |
    kapp deploy \
      --namespace default \
      --app git-secret \
      --diff-changes \
      --yes \
      --file -

kubectl patch serviceaccount default \
  --namespace apps \
  --type merge \
  --patch '{"secrets": [{"name": "git-secret"}]}'
