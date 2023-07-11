#!/usr/bin/env bash

set -euo pipefail

CLUSTER_NAME=spring-authserver

if ! type kind 2>/dev/null; then
  echo -e "
!! kind is not installed, please install kind

See: https://kind.sigs.k8s.io/docs/user/quick-start/#installation

On macOS:

  brew install kind
"
  exit 1
fi

if ! type kapp 2>/dev/null; then
  echo -e "
!! kapp is not installed, please install kapp

See: https://carvel.dev/kapp/docs/v0.57.0/install/

On macOS:

  brew tap vmware-tanzu/carvel && brew install kapp
"
  exit 1
fi

if ! kind get clusters | grep "$CLUSTER_NAME"; then
  echo "~~ Setting up kind cluster"
  kind create cluster --config kind.yml --name "$CLUSTER_NAME"
  echo "~~ Setting up kind cluster > done"
fi

echo "~~ Updating coredns to support .nip.io"
kubectl apply -f coredns-cm.yml
kubectl rollout restart deploy/coredns -n kube-system
echo "~~ Updating coredns to support .nip.io > done"

echo "~~ Setting nginx ingress"
if kapp inspect -a nginx-ingress >/dev/null; then
  echo "~~ Setting nginx ingress > already installed, skipping"
else
  kapp deploy -a nginx-ingress -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/main/deploy/static/provider/kind/deploy.yaml -y
  echo "~~ Setting nginx ingress > done"
fi
