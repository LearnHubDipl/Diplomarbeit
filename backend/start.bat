echo Stopping existing containers...
docker-compose down
if errorlevel 1 (
  echo Fehler beim Stoppen der Container
)

echo Building and starting containers...
docker-compose up --build 
if errorlevel 1 (
  echo Fehler beim Starten der Container
)

echo Done!
