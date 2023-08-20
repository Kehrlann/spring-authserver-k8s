#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

eval $(source "$SCRIPT_DIR/get-client-registration-secret.sh")

RESPONSE=$(curl "$ISSUER_URI/oauth2/token" \
  --data "grant_type=client_credentials" \
  --user "$CLIENT_ID:$CLIENT_SECRET" \
  --silent)
echo -e "~~ Token response:\n$RESPONSE\n\n"

TOKEN=$(jq ".access_token" <<< "$RESPONSE")
echo -e "~~ Token value:\n$RESPONSE\n\n"

echo "~~ Decoded token:"
"$SCRIPT_DIR/decode-jwt.sh" "$TOKEN"
