#!/bin/sh
set -e

# Start a virtual framebuffer (no physical display required)
Xvfb :99 -screen 0 1280x800x24 -nolisten tcp &
sleep 1

# Start VNC server on port 5900 with no password
x11vnc -display :99 -nopw -listen 0.0.0.0 -xkb -noxdamage -forever -bg -quiet

# Point JavaFX at the virtual display and launch the app
export DISPLAY=:99
exec java \
  --module-path /app/javafx \
  --add-modules javafx.controls,javafx.fxml \
  -jar /app/app.jar
