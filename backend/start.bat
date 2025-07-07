@echo off
REM Skript bei Fehler abbrechen (Batch hat kein direktes Äquivalent, also manuell prüfen)

echo Stopping existing containers...
docker-compose down
if errorlevel 1 (
  echo Fehler beim Stoppen der Container
  exit /b 1
)

echo Building and starting containers...
docker-compose up --build -d
if errorlevel 1 (
  echo Fehler beim Starten der Container
  exit /b 1
)

echo Done!
