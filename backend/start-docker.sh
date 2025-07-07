#!/bin/bash
set -e  # Skript bei Fehler abbrechen

echo "Stopping existing containers..."
docker-compose down

echo "Building and starting containers..."
docker-compose up --build -d

echo "Done!"
