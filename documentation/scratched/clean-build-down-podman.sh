#!/bin/bash

echo "Starting MySQL container only..."
podman-compose up -d mysql

echo "Waiting a few seconds for MySQL to initialize..."
sleep 15  # adjust if needed for your MySQL startup time

echo "Cleaning and building Maven project..."
./mvnw clean package

if [ $? -ne 0 ]; then
  echo "Maven build failed. Aborting."
  podman-compose down
  exit 1
fi

echo "Building and starting all containers..."
podman-compose up --build -d

echo "Stopping and removing all containers..."
podman-compose down
