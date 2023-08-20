#!/usr/bin/env bash

set -euo pipefail

MY_SECRET=demo-client
MY_NS=apps

export CLIENT_ID=$(kubectl get secret $MY_SECRET -n $MY_NS -o jsonpath="{.data.client-id}" | base64 -d)
export CLIENT_SECRET=$(kubectl get secret $MY_SECRET -n $MY_NS -o jsonpath="{.data.client-secret}" | base64 -d)
export ISSUER_URI=$(kubectl get secret $MY_SECRET -n $MY_NS -o jsonpath="{.data.issuer-uri}" | base64 -d)
export REDIRECT_URI=$(kubectl get clientregistration $MY_SECRET -n $MY_NS -o jsonpath="{.spec.redirectURIs[0]}")

echo "
export CLIENT_ID=$(kubectl get secret $MY_SECRET -n $MY_NS -o jsonpath="{.data.client-id}" | base64 -d)
export CLIENT_SECRET=$(kubectl get secret $MY_SECRET -n $MY_NS -o jsonpath="{.data.client-secret}" | base64 -d)
export ISSUER_URI=$(kubectl get secret $MY_SECRET -n $MY_NS -o jsonpath="{.data.issuer-uri}" | base64 -d)
export REDIRECT_URI=$(kubectl get clientregistration $MY_SECRET -n $MY_NS -o jsonpath="{.spec.redirectURIs[0]}")
"
