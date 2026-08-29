#!/bin/sh
# Clean
cd "$JCODE_PROJECT_DIR" || exit 1
echo "== flutter clean =="
flutter clean
