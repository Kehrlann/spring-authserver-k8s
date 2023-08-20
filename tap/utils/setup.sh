#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

kubectl apply --filename "$SCRIPT_DIR/../clientregistration.yml"
kubectl apply --filename "$SCRIPT_DIR/../workload.yml" --filename "$SCRIPT_DIR/../classclaim.yml"
