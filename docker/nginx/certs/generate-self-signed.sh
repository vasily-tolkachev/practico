#!/usr/bin/env sh
set -eu

DOMAIN="${1:-localhost}"
DAYS="${2:-365}"

SCRIPT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
KEY_PATH="$SCRIPT_DIR/privkey.pem"
CERT_PATH="$SCRIPT_DIR/fullchain.pem"

openssl req -x509 -nodes -newkey rsa:2048 \
  -keyout "$KEY_PATH" \
  -out "$CERT_PATH" \
  -days "$DAYS" \
  -subj "/CN=$DOMAIN"

echo "Generated:"
echo "  $CERT_PATH"
echo "  $KEY_PATH"
