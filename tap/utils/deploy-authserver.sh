#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

TAP_VALUES_FILE=$(bash "$SCRIPT_DIR/get-tap-values.sh")

INGRESS_DOMAIN=$(yq ".shared.ingress_domain" "$TAP_VALUES_FILE")
AZURE_DATA_VALUES_FLAG=""
if [[ -f "$SCRIPT_DIR/../azure-data-values.yml" ]]; then
  AZURE_DATA_VALUES_FLAG="--data-values-file $SCRIPT_DIR/../azure-data-values.yml"
fi

ytt \
  --file "$SCRIPT_DIR/../authserver-dex-idp.yml" \
  --file "$SCRIPT_DIR/../authserver.yml" \
  $AZURE_DATA_VALUES_FLAG \
  --data-value "domain=$INGRESS_DOMAIN" |
    kapp deploy \
      --namespace default \
      --app authserver \
      --diff-changes \
      --yes \
      --file -
