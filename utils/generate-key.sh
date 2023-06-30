#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
readonly usage=${KEY_USAGE:-"signing"}

tmp_dir=$(mktemp -d /tmp/keys.XXX)
openssl genrsa -out "$tmp_dir/key.pem" 2048 2>/dev/null
ytt -f "$SCRIPT_DIR/key-template.yml" --data-value key_pem="$(cat ${tmp_dir}/key.pem)" --data-value uuid="$(date +%H%M-%S)" --data-value usage="$usage"

