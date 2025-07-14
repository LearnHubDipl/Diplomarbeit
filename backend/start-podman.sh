#!/bin/bash
set -e

echo "Stopping existing containers..."
podman-compose down

echo "Building and starting containers..."
podman-compose up --build

echo "Done!"
