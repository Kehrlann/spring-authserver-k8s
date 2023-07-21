#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

kubectl apply -f "$SCRIPT_DIR/../namespace.yml"
source "$SCRIPT_DIR/deploy-dex.sh"
source "$SCRIPT_DIR/deploy-authserver.sh"
kapp deploy --app clientreg --namespace default -f "$SCRIPT_DIR/../clientregistration.yml" --yes
kapp deploy --app workload --namespace default -f "$SCRIPT_DIR/../workload.yml" -f "$SCRIPT_DIR/../classclaim.yml"  --yes

