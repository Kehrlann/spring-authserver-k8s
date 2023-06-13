#!/usr/bin/env bash

set -euo pipefail

curl -XPOST http://authserver.127.0.0.1.nip.io/oauth2/token -d grant_type=client_credentials -u "test-client:test-secret" | jq
