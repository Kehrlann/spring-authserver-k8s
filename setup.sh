#!/usr/bin/env bash

set -euo pipefail

CLUSTER_NAME=spring-authserver

if ! kind get clusters | grep "$CLUSTER_NAME"; then
  echo "~~ Setting up kind cluster"
  kind create cluster --config kind.yml --name "$CLUSTER_NAME"
  echo "~~ Setting up kind cluster > done"
fi

echo "~~ Setting nginx ingress"
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/main/deploy/static/provider/kind/deploy.yaml

kubectl wait --namespace ingress-nginx \
  --for=condition=ready pod \
  --selector=app.kubernetes.io/component=controller \
  --timeout=90s

echo "~~ Setting nginx ingress > done"
