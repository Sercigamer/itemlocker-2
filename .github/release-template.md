> Clientseitige Fabric-Mod für Minecraft {{MC_VERSION}}.
> Gesperrte Items fallen nicht beim ersten Drop-Versuch.

{{CHANGELOG}}

## Installation

1. [Fabric Loader](https://fabricmc.net/use/installer/) für **{{MC_VERSION}}** installieren
2. [Fabric API](https://modrinth.com/mod/fabric-api) für {{MC_VERSION}} in den `mods`-Ordner legen — **zwingend nötig**
3. `{{JAR}}` daneben legen
4. Optional: [Mod Menu](https://modrinth.com/mod/modmenu) für den Zahnrad-Knopf im Mod-Menü

**Lunar Client:** beide Jars nach `%USERPROFILE%\.lunarclient\mods\{{MC_VERSION}}`, dann Lunar neu starten.

## Tastenbelegung

| Taste | Funktion |
| --- | --- |
| `L` | Aktuellen Hotbar-Slot sperren / entsperren |
| `K` | Item-Typ in der Hand sperren / entsperren |
| *frei belegbar* | ItemLocker komplett an/aus |
| *frei belegbar* | Config-Menü öffnen |

Alle vier stehen unter **Optionen → Steuerung → Tastenbelegung → Inventar** und lassen sich frei ändern. Die beiden unteren sind absichtlich unbelegt, damit die Mod dir keine Taste wegnimmt.

## Schnellstart

| Aktion | Wie |
| --- | --- |
| Config-Menü öffnen | `/itemlocker config` |
| Item in der Hand sperren | Taste `K` |
| Hotbar-Slot sperren | Taste `L` |
| Alle Befehle anzeigen | `/itemlocker help` |

## Wenn nichts passiert

Fast immer fehlt **Fabric API** im `mods`-Ordner. Zur Kontrolle Minecraft einmal mit `-Ditemlocker.selftest=true` starten — dann steht in `logs/latest.log` pro Einhängepunkt eine `Selbsttest OK`-Zeile.

---

Volle Anleitung im [README](https://github.com/{{REPO_FULL}}#readme) · [Changelog](https://github.com/{{REPO_FULL}}/blob/main/CHANGELOG.md)
