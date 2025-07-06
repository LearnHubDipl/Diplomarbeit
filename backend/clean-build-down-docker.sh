#!/bin/bash

echo "Starting MySQL container only..."
docker-compose up -d mysql

echo "Waiting a few seconds for MySQL to initialize..."
sleep 15  # adjust if needed for your MySQL startup time

echo "Cleaning and building Maven project..."
./mvnw clean package

if [ $? -ne 0 ]; then
  echo "Maven build failed. Aborting."
  docker-compose down
  exit 1
fi

echo "Building and starting all containers..."
docker-compose up --build -d

echo "Stopping and removing all containers..."
docker-compose down
