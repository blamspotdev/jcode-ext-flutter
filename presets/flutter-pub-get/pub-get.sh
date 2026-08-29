#!/bin/sh
# Get packages
cd "$JCODE_PROJECT_DIR" || exit 1
echo "== flutter pub get =="
flutter pub get
