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
# `flutter create`'s own two switches, and the whole of what separates the gallery's five entries.
# Defaulted so the template still scaffolds an app when it is run without them -- from JCode's plain
# New Project dialog, or by hand.
TEMPLATE="${JCODE_INPUT_TEMPLATE:-app}"

# `--platforms` is an app's argument. A package has no platform half at all, and passing it there is
# an error rather than a no-op.
case "$TEMPLATE" in
  package) set -- ;;
  *) set -- --platforms "$PLATFORMS" ;;
esac
# `--empty` leaves out the counter demo. It is only meaningful for an app.
if [ -n "${JCODE_INPUT_EMPTY:-}" ] && [ "$TEMPLATE" = app ]; then
  set -- "$@" --empty
fi

echo "== Creating a Flutter project =="
echo "   template:     $TEMPLATE"
echo "   package name: $NAME"
echo "   organisation: $ORG"
echo

cd "$JCODE_PROJECT_DIR"
# `.` rather than a new directory: JCode has already made the project directory and is watching it.
flutter create --project-name "$NAME" --org "$ORG" --template "$TEMPLATE" "$@" .

mkdir -p "$JCODE_PROJECT_DIR/.jcode"
