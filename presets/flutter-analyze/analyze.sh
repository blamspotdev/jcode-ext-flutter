#!/bin/sh
# Analyze
cd "$JCODE_PROJECT_DIR" || exit 1
echo "== flutter analyze =="
flutter analyze
