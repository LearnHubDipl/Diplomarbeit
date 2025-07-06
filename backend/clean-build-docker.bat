@echo off
echo Starting MySQL container only...
docker-compose up -d mysql

echo Waiting 15 seconds for MySQL to initialize...
timeout /t 15

echo Cleaning and building Maven project...
call mvnw.cmd clean package
if errorlevel 1 (
  echo Maven build failed. Aborting.
  docker-compose down
  exit /b 1
)

echo Building and starting all containers...
docker-compose up --build -d

echo Stopping and removing all containers...
docker-compose down
