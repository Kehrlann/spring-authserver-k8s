#!/usr/bin/env bash

set -euo pipefail

kubectl delete secret -n default --field-selector "type=authserver/jwk" --field-selector "metadata.name!=default-key"
