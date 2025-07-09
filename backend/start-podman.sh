#!/bin/bash
set -e  # Skript bei Fehler abbrechen

echo "Stopping existing containers..."
podman-compose down

echo "Building and starting containers..."
podman-compose up --build

echo "Done!"
