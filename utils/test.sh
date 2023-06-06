#!/usr/bin/env bash

set -euo pipefail

kubectl apply -f nginx.yml

kubectl wait --namespace nginx \
  --for=condition=ready pod \
  --selector=app=nginx \
  --timeout=90s

for i in {1..5}; do
  s=0
  curl nginx.127.0.0.1.nip.io --fail && break ||
    s=$? && echo "Waiting for nginx to come up ..." && sleep 1
done

if [[ $s != 0 ]]; then
  echo "Could not reach nginx deployment..."
  exit $s
fi


kubectl delete -f nginx.yml

echo -e "\n~~ Success!"

