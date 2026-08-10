# Changelog

Alle nennenswerten Änderungen an ItemLocker.

## [1.3.0]

### Neu

- **Rüstungsständer-Schutz**: Gesperrte Sachen lassen sich nicht mehr an einen Rüstungsständer anlegen. Hier wird nicht gezählt, sondern hart blockiert — ein einziger Rechtsklick genügt sonst, und das Item hängt am Ständer.
- Neue Option `protectArmorStands` (Standard: an), umschaltbar im Config-Menü und über `/itemlocker armorstands <true|false>`.

### Geändert

- Der eingebaute Mixin-Selbsttest prüft den Methodennamen jetzt als Endung statt als Teilstring. Vorher konnte eine Prüfung fälschlich durch einen längeren Namen erfüllt werden.

### Behoben

- Die Repo-Links in der Mod-Info (`fabric.mod.json`) zeigten auf ein Repository mit falschem Namen. Benutzer und Repo-Name kommen jetzt beide aus `gradle.properties`.

## [1.2.1]

### Behoben

- Im **Kreativmodus** fielen gesperrte Items beim ersten Versuch, sobald das Inventar offen war. Das Kreativ-Inventar dropt nicht über `clickSlot`, sondern über `ClientPlayerInteractionManager.dropCreativeStack` — dieser Weg war ungeschützt. Im Survival war der Schutz nie betroffen.

## [1.2.0]

### Behoben

- Ein gesperrter Hotbar-Slot ließ sich im Inventar umgehen: Item mit der Maus aufnehmen, neben das Fenster klicken, weg war es. Sobald das Item den Slot verlässt, konnte die Slot-Sperre nicht mehr greifen.
- Die Zahlentasten-Aktion (`SlotActionType.SWAP`) wurde nicht geprüft — dabei steckt der betroffene Hotbar-Slot im `button`-Parameter, nicht in `slotId`.

### Geändert

- `preventTakingFromLockedSlots` ist jetzt Standard: der Inhalt eines gesperrten Slots bleibt im Inventar an Ort und Stelle. Abschaltbar im Config-Menü.
- Bestehende Konfigurationsdateien werden beim Start automatisch auf Version 1 gehoben.

## [1.1.0]

### Neu

- Config-Menü mit allen Optionen als Schalter und Schieberegler
- Item-Auswahl: durchsuchbare Liste aller Items, Klick sperrt oder entsperrt; Filter für alle Items, nur gesperrte oder nur die aus dem Inventar
- Slot-Auswahl: die neun Hotbar-Slots als anklickbare Reihe
- Anbindung an [Mod Menu](https://modrinth.com/mod/modmenu) (optional)
- Erreichbar zusätzlich über `/itemlocker config` und eine eigene Taste

## [1.0.0]

### Neu

- Hotbar-Slots und Item-Typen sperren; gesperrte Sachen fallen erst nach mehreren Drop-Versuchen (Standard 5)
- Schutz auch in Inventar-Bildschirmen
- Schloss-Symbole über der Hotbar samt Fortschrittsbalken
- Befehle `/itemlocker` und `/il`, Tasten für Slot- und Item-Sperre
- Konfiguration unter `config/itemlocker.json`
