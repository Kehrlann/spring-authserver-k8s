#!/usr/bin/env bash

# See: https://gist.github.com/thomasdarimont/46358bc8167fce059d83a1ebdb92b0e7
# Useful script for decoding JWT tokens in the browser, used in combination
# with get-token
#
# Example:
# echo "<some-jwt>" | ./scripts/decode-jwt.sh

decode_base64_url() {
  local len=$((${#1} % 4))
  local result="$1"
  if [ $len -eq 2 ]; then
    result="$1"'=='
  elif [ $len -eq 3 ]; then
    result="$1"'='
  fi
  echo "$result" | tr '_-' '/+' | base64 -D
}

decode_jwt() {
  local input=$1
  if [[ -z $input ]]; then
    while read -r data; do
      input+=$data
    done
  fi

  decode_base64_url "$(echo -n "$input" | cut -d "." -f 1)" | jq .
  decode_base64_url "$(echo -n "$input" | cut -d "." -f 2)" | jq .
}

decode_jwt "$@"
