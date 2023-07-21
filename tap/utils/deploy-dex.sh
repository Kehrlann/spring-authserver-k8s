#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

TAP_VALUES_FILE=$(bash "$SCRIPT_DIR/get-tap-values.sh")

INGRESS_DOMAIN=$(yq ".shared.ingress_domain" "$TAP_VALUES_FILE")
INGRESS_ISSUER=$(yq ".shared.ingress_issuer" "$TAP_VALUES_FILE")


ytt --file "$SCRIPT_DIR/../dex.yml" \
  --data-value "domain=$INGRESS_DOMAIN" \
  --data-value "issuer=$INGRESS_ISSUER" |
    kapp deploy \
      --namespace default \
      --app dex \
      --diff-changes \
      --yes \
      --file -
