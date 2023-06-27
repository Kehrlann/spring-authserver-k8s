#!/usr/bin/env bash

set -euo pipefail

CLUSTER_NAME=spring-authserver

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
kapp deploy -a nginx-ingress -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/main/deploy/static/provider/kind/deploy.yaml -y
echo "~~ Setting nginx ingress > done"

echo "~~ Setting secretgen-controller"
kapp deploy -a secretgen-controller -f https://github.com/carvel-dev/secretgen-controller/releases/latest/download/release.yml -y
echo "~~ Setting secretgen-controller > done"
