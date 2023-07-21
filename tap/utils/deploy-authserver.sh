#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

TAP_VALUES_FILE=$(bash "$SCRIPT_DIR/get-tap-values.sh")

INGRESS_DOMAIN=$(yq ".shared.ingress_domain" "$TAP_VALUES_FILE")

ytt --file "$SCRIPT_DIR/../authserver.yml" \
  --data-value "domain=$INGRESS_DOMAIN" |
    kapp deploy \
      --namespace default \
      --app authserver \
      --diff-changes \
      --yes \
      --file -
