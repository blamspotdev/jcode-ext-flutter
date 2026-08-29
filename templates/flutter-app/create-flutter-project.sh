#!/bin/sh
# Create the Flutter project
set -e

# A Dart package name, not a directory name. `flutter create` refuses anything that is not a valid
# Dart identifier — dashes, capitals and a leading digit are all ordinary in a project name here and
# all rejected there, so the name is folded rather than passed through and left to fail at the end
# of a long scaffold.
NAME="$(printf '%s' "$JCODE_PROJECT_NAME" | tr '[:upper:]' '[:lower:]' | tr -c 'a-z0-9_' '_')"
case "$NAME" in
  [0-9]*) NAME="app_$NAME" ;;
esac
[ -n "$NAME" ] || NAME=flutter_app

ORG="${JCODE_INPUT_ORG:-com.example}"
PLATFORMS="${JCODE_INPUT_PLATFORMS:-android}"

echo "== Creating a Flutter app =="
echo "   package name: $NAME"
echo "   organisation: $ORG"
echo "   platforms:    $PLATFORMS"
echo

cd "$JCODE_PROJECT_DIR"
# `.` rather than a new directory: JCode has already made the project directory and is watching it.
flutter create --project-name "$NAME" --org "$ORG" --platforms "$PLATFORMS" .

mkdir -p "$JCODE_PROJECT_DIR/.jcode"
