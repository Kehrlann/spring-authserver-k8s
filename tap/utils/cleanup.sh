#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

function delete_if_exists() {
  local -r app_name=$1
  echo "Deleting $1..."
  if kapp inspect --app $1 --namespace default 2>&1 >/dev/null; then
    kapp delete --app $1 --namespace default -y
    echo "Deleting $1... Done!"
  else
    echo "Deleting $1... Does not exist, skipping"
  fi
}

delete_if_exists workload
delete_if_exists clientreg
delete_if_exists authserver
