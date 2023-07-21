#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

kctrl package installed get \
  --package-install tap \
  --namespace tap-install \
  --values-file-output "$SCRIPT_DIR/tap-values.yml" >/dev/null

echo "$SCRIPT_DIR/tap-values.yml"
