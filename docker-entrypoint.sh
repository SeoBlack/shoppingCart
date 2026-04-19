#!/bin/sh
set -e

if [ -n "$DISPLAY" ]; then
  echo "Using host X11 display: $DISPLAY"
  if ! xdpyinfo >/dev/null 2>&1; then
    echo "Cannot connect to X server at DISPLAY=$DISPLAY"
    echo "Make sure Xming is running with TCP listening and access control disabled."
    exit 1
  fi
else
  echo "No DISPLAY provided, starting virtual display + VNC"
  Xvfb :99 -screen 0 1280x800x24 -nolisten tcp &
  sleep 1
  x11vnc -display :99 -nopw -listen 0.0.0.0 -xkb -noxdamage -forever -bg -quiet
  export DISPLAY=:99
fi

exec java \
  -Dprism.order=sw \
  --module-path /app/javafx \
  --add-modules javafx.controls,javafx.fxml \
  -cp "/app/app.jar:/app/libs/*" \
  org.example.Main