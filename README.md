# PopcornSMP Claim Plugin

Dieses Projekt enthält ein Spigot-Plugin für den **PopcornSMP** Server. Spieler können darüber bis zu zehn Chunks beanspruchen, verwalten und schützen. Alle Funktionen stehen sowohl über das `/chunk`-Menü als auch über Befehle zur Verfügung.

## Features

- Claim-Limit von 10 Chunks pro Spieler
- GUI-Menü mit folgenden Optionen:
  - Aktuellen Chunk hervorheben (15 Sekunden Partikel-Rahmen)
  - Chunk claimen oder freigeben
  - Verwaltung mit Claim-Liste und Vertrauensverwaltung
- Befehle zur direkten Steuerung (`/chunk claim`, `/chunk unclaim`, `/chunk trust <Spieler>`, ...)
- Vertrauenssystem: hinzugefügte Spieler erhalten Zugriff auf alle Claims
- Schutz vor Interaktionen, Explosionen, Flüssigkeiten und Piston-Mechaniken durch nicht berechtigte Spieler

## Entwicklung

1. **Bauen:**
   ```bash
   mvn package
   ```
2. Die fertige Jar-Datei befindet sich anschließend im Verzeichnis `target/`.
3. Kopiere die Jar-Datei in den `plugins/`-Ordner deines Spigot/Paper-Servers und starte ihn neu.

Die gespeicherten Claims liegen in der Datei `plugins/PopcornSMPClaim/claims.yml`.
