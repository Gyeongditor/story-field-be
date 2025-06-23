#!/usr/bin/env bash
# wait-for-it.sh

set -e

host_port="$1"
shift

host=$(echo "$host_port" | cut -d: -f1)
port=$(echo "$host_port" | cut -d: -f2)

timeout=15

while [[ "$1" != "" ]]; do
  case "$1" in
    --timeout)
      shift
      timeout="$1"
      ;;
    --)
      shift
      break
      ;;
  esac
  shift
done

echo "Waiting for $host:$port..."

for i in $(seq $timeout); do
  if nc -z "$host" "$port"; then
    echo "✅ $host:$port is available"
    exec "$@"
    exit 0
  fi
  sleep 1
done

echo "❌ Timeout waiting for $host:$port"
exit 1
