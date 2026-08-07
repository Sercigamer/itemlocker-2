# ItemLocker

Eine clientseitige Fabric-Mod für **Minecraft 1.21.11**, die verhindert, dass du aus Versehen wichtige Items wegwirfst.

Du sperrst einzelne **Hotbar-Slots** oder ganze **Item-Typen**. Ein gesperrtes Item fällt nicht beim ersten Druck auf die Drop-Taste, sondern erst, wenn du sie **5-mal hintereinander** drückst (Anzahl frei einstellbar, 1–64).

---

## Features

- 🔒 **Hotbar-Slots sperren** – Slot 1–9 einzeln, egal was drin liegt
- 🗡️ **Item-Typen sperren** – z. B. `minecraft:elytra` ist überall geschützt
- 🖥️ **Config-Menü** – alles per Maus einstellbar, inkl. durchsuchbarer Item-Liste (mit [Mod Menu](https://modrinth.com/mod/modmenu) oder ohne)
- 🔁 **Mehrfach-Drop** – gesperrte Items fallen erst nach *N* Versuchen (Standard: 5)
- 📦 **Auch im Inventar** – `Q` auf einem Slot und das Rauswerfen des Stacks am Cursor sind ebenfalls geschützt
- 🧊 **Einfrieren (optional)** – gesperrte Hotbar-Slots lassen sich im Inventar gar nicht mehr verschieben
- 👀 **HUD-Anzeige** – kleines Schloss auf gesperrten Slots plus Fortschrittsbalken beim Droppen
- ⌨️ **Tasten & Commands** – alles ohne Datei-Editieren einstellbar
- 🌍 Deutsch und Englisch

Die Mod ist **rein clientseitig**. Der Server bekommt einen geblockten Drop nie zu sehen, weil das Paket gar nicht erst rausgeht – funktioniert also auch auf Servern, die keine Mods erlauben.

---

## Installation

### Normaler Fabric-Launcher

1. [Fabric Loader](https://fabricmc.net/use/installer/) für **1.21.11** installieren
2. [Fabric API](https://modrinth.com/mod/fabric-api) (Version für 1.21.11) in den `mods`-Ordner legen
3. `itemlocker-1.1.0.jar` daneben legen
4. Optional: [Mod Menu](https://modrinth.com/mod/modmenu) für den Zahnrad-Knopf im Mod-Menü
4. Minecraft mit dem Fabric-Profil starten

### Lunar Client

Lunar Client lädt Fabric-Mods aus einem eigenen Ordner:

```
%USERPROFILE%\.lunarclient\mods\1.21.11
```

Dort **beide** Jars ablegen — `itemlocker-*.jar` **und** `fabric-api-*.jar` für 1.21.11. Danach Lunar neu starten und die Mod unter *Settings → Mods* aktivieren.

> **Hinweis:** Lunar Client bringt kein Fabric API mit. Ohne Fabric API startet die Mod nicht.
> Falls Lunar noch kein 1.21.11 anbietet, passe `minecraft_version`, `yarn_mappings` und `fabric_version` in [`gradle.properties`](gradle.properties) auf die von Lunar unterstützte Version an und baue neu.

---

## Bedienung

### Config-Menü

Drei Wege dorthin:

1. **Mod Menu** – im Mod-Menü auf ItemLocker → Zahnrad
2. **Command** – `/itemlocker config`
3. **Taste** – „ItemLocker-Menü öffnen" in den Steuerungs-Einstellungen belegen

Mod Menu ist **optional**: ohne funktionieren Command und Taste trotzdem.

![Config-Menü](docs/config-screen.png)

Im Hauptmenü stellst du alle Optionen per Maus ein (Mod an/aus, nötige Drops als Schieberegler, Sound, Meldungen, HUD …). Dazu zwei Unterseiten:

- **Hotbar-Slots …** – die neun Slots als anklickbare Reihe, inklusive Vorschau, was gerade drinliegt
- **Items … (N)** – durchsuchbare Liste aller Items. Tippe z. B. `elytra`, klicke die Zeile an, fertig. Über den Filter oben rechts wechselst du zwischen **Alle Items**, **Nur gesperrte** und **Aus Inventar** (zeigt nur, was du gerade dabei hast).

![Item-Auswahl](docs/item-screen.png)

![Hotbar-Slots](docs/slot-screen.png)

### Tasten (in den Steuerungs-Einstellungen unter „Inventar" änderbar)

| Taste | Funktion |
| --- | --- |
| `L` | Aktuellen Hotbar-Slot sperren / entsperren |
| `K` | Item-Typ in der Hand sperren / entsperren |
| — | ItemLocker komplett an/aus (standardmäßig nicht belegt) |

### Commands (`/itemlocker`, kurz `/il`)

| Command | Beschreibung |
| --- | --- |
| `/itemlocker` | Status anzeigen |
| `/itemlocker help` | Alle Befehle |
| `/itemlocker slot <1-9>` | Hotbar-Slot umschalten |
| `/itemlocker slot <1-9> lock\|unlock` | Slot gezielt sperren / entsperren |
| `/itemlocker item` | Item in der Hand umschalten |
| `/itemlocker item <id>` | Item per Name, z. B. `minecraft:totem_of_undying` |
| `/itemlocker count <1-64>` | Wie oft gedroppt werden muss (Standard 5) |
| `/itemlocker timeout <1-60>` | Nach wie vielen Sekunden der Zähler zurückgesetzt wird |
| `/itemlocker list` | Alle Sperren anzeigen |
| `/itemlocker clear` | Alle Sperren löschen |
| `/itemlocker on` / `off` | Mod an- / ausschalten |
| `/itemlocker hud <true\|false>` | Schloss-Symbole im HUD |
| `/itemlocker sound <true\|false>` | Warn-Sound |
| `/itemlocker messages <true\|false>` | Text über der Hotbar |
| `/itemlocker inventory <true\|false>` | Schutz auch in Inventar-GUIs |
| `/itemlocker freeze <true\|false>` | Gesperrte Slots im Inventar komplett einfrieren |

---

## Config

Wird automatisch angelegt unter `config/itemlocker.json`:

```json
{
  "enabled": true,
  "requiredDrops": 5,
  "resetAfterMillis": 3000,
  "guardInventoryScreens": true,
  "preventTakingFromLockedSlots": false,
  "showHudIcons": true,
  "playSound": true,
  "actionBarMessages": true,
  "lockedSlots": [0, 8],
  "lockedItems": ["minecraft:elytra"]
}
```

`lockedSlots` sind **0-basiert** (0 = Slot 1 auf der Tastatur, 8 = Slot 9). In Spiel und Commands wird 1–9 verwendet.

`resetAfterMillis` ist das Zeitfenster: Wer 3 Sekunden lang nicht droppt, fängt beim nächsten Versuch wieder bei 1 an. So zählt ein Fehldruck von vorhin nicht später mit.

---

## Selbst bauen

Benötigt **JDK 21** (Gradle lädt es bei Bedarf automatisch herunter).

```bash
./gradlew build
```

Das fertige Jar liegt danach unter `build/libs/itemlocker-1.1.0.jar` (die Datei mit `-sources` im Namen wird nicht gebraucht).

Zum Testen mit einem Entwicklungs-Client:

```bash
./gradlew runClient
```

---

## Wenn nichts passiert

Die Mod hakt sich per Mixin in den Client ein. Falls das Droppen sich weiter wie in Vanilla verhält, starte Minecraft einmal mit

```
-Ditemlocker.selftest=true
```

in den JVM-Argumenten. Im Log (`logs/latest.log`) steht dann pro Einhängepunkt eine Zeile:

- `Selbsttest OK: ... enthaelt itemlocker$guardHotbarDrop` → alles gut, die Mod greift
- `Selbsttest FEHLGESCHLAGEN: ...` → die Mixins wurden nicht angewendet (meist: falsche Minecraft-Version oder der Launcher lädt keine Mixins)

Häufigste Ursache auf Lunar Client: **Fabric API fehlt** im `mods`-Ordner.

---

## GitHub

Das Repo ist bereits vorbereitet:

- `.github/workflows/build.yml` – baut bei jedem Push und hängt das Jar als Artifact an
- `.github/workflows/release.yml` – erstellt bei einem Tag `v*` automatisch ein Release mit Jar
- `.gitignore`, `.gitattributes`, `LICENSE` (MIT)

Repo verbinden:

```bash
git remote add origin https://github.com/DEIN-NAME/itemlocker.git
```

Danach pushen:

```bash
git push -u origin main
```

Release veröffentlichen:

```bash
git tag v1.0.0 && git push origin v1.0.0
```

Denk daran, in [`fabric.mod.json`](src/main/resources/fabric.mod.json) die Platzhalter-URLs (`USERNAME`) durch deinen GitHub-Namen zu ersetzen.

---

## Lizenz

MIT – siehe [LICENSE](LICENSE).
