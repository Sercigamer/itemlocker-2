<div align="center">

# 🔒 ItemLocker

**Nie wieder aus Versehen die Elytra weggeworfen.**

Eine clientseitige Fabric-Mod, die deine wichtigsten Items festhält:
gesperrte Sachen fallen erst, wenn du die Drop-Taste **mehrfach hintereinander** drückst.

[![Build](https://github.com/Sercigamer/itemlocker-2/actions/workflows/build.yml/badge.svg)](https://github.com/Sercigamer/itemlocker-2/actions/workflows/build.yml)
![Minecraft](https://img.shields.io/badge/Minecraft-26.2-brightgreen)
![Loader](https://img.shields.io/badge/Loader-Fabric-dbd0b4)
![Seite](https://img.shields.io/badge/Seite-nur%20Client-blue)
![Lizenz](https://img.shields.io/badge/Lizenz-MIT-lightgrey)

</div>

---

## Inhalt

- [Das Problem](#das-problem)
- [Installation](#installation)
- [In 60 Sekunden loslegen](#in-60-sekunden-loslegen)
- [Die zwei Sperrarten](#die-zwei-sperrarten)
- [Was genau geschützt ist](#was-genau-geschützt-ist)
- [Das Config-Menü](#das-config-menü)
- [Tasten](#tasten)
- [Befehle](#befehle)
- [Die Anzeige über der Hotbar](#die-anzeige-über-der-hotbar)
- [Die Config-Datei](#die-config-datei)
- [Wie es funktioniert](#wie-es-funktioniert)
- [Kompatibilität](#kompatibilität)
- [Wenn nichts passiert](#wenn-nichts-passiert)
- [Selbst bauen](#selbst-bauen)
- [Auf GitHub veröffentlichen](#auf-github-veröffentlichen)

---

## Das Problem

`Q` liegt direkt neben `W`, `A`, `S` und `E`. Ein Fehlgriff im Kampf, und die Elytra liegt auf dem Boden — im Zweifel über Lava.

ItemLocker legt einen Riegel davor. Du markierst, was dir wichtig ist, und ab dann braucht ein Drop **fünf Versuche statt einem** (frei einstellbar, 1–64). Ein Fehlgriff reicht nicht mehr. Absichtlich wegwerfen kannst du trotzdem — du musst es nur ernst meinen.

Dazu kommen harte Sperren für Wege, auf denen ein Item ohne Drop verschwindet: aus einem gesperrten Slot herausziehen, an einen **Rüstungsständer** anlegen, in einen **Deko-Topf** stecken oder in die **Zweithand** tauschen. Und eine **Block-Sperre** gegen die Endertruhe, auf die man mitten im Kampf klickt.

---

## Installation

Du brauchst immer: **Minecraft 26.2**, **Fabric Loader**, **Fabric API** und **Java 25+**.

### Normaler Fabric-Launcher

1. [Fabric Loader](https://fabricmc.net/use/installer/) für 26.2 installieren
2. [Fabric API](https://modrinth.com/mod/fabric-api) für 26.2 in den `mods`-Ordner
3. `itemlocker-2.0.1+26.2.jar` daneben legen
4. *Optional:* [Mod Menu](https://modrinth.com/mod/modmenu) für den Zahnrad-Knopf im Mod-Menü

Der `mods`-Ordner liegt unter `%APPDATA%\.minecraft\mods` (Windows) bzw. `~/.minecraft/mods` (Linux) oder `~/Library/Application Support/minecraft/mods` (macOS).

### Lunar Client

Lunar hat einen eigenen Ordner pro Version:

```
%USERPROFILE%\.lunarclient\mods\26.2
```

Dort **beide** Jars ablegen — `itemlocker-*.jar` **und** `fabric-api-*.jar`. Danach Lunar neu starten und die Mod unter *Settings → Mods* aktivieren.

> **Wichtig:** Lunar bringt kein Fabric API mit. Fehlt es, startet die Mod nicht — und du siehst keine Fehlermeldung im Spiel, nur einen Eintrag im Log.

### Prism / MultiMC / ATLauncher

Instanz mit Fabric für 26.2 anlegen, dann beide Jars über *Mods hinzufügen* einbinden.

---

## In 60 Sekunden loslegen

1. Nimm das Item in die Hand, das geschützt werden soll
2. Drücke **`K`** → der Item-Typ ist gesperrt
3. Fertig. Über der Hotbar erscheint ein kleines Schloss.

Zum Ausprobieren: Drop-Taste drücken. Statt zu fallen erscheint über der Hotbar
`Elytra gesperrt – noch 4× droppen (von 5)`. Erst beim fünften Druck fällt sie.

Alles Weitere stellst du im Menü ein: `/itemlocker config`

---

## Die zwei Sperrarten

Das ist der wichtigste Punkt zum Verstehen — die beiden Arten verhalten sich bewusst unterschiedlich.

### 🔴 Slot-Sperre (Taste `L`)

Sperrt **einen Hotbar-Platz**, egal was darin liegt. Slot 1 gesperrt heißt: Was auch immer auf Platz 1 liegt, ist geschützt. Tauschst du den Inhalt aus, ist automatisch das neue Item geschützt.

Zusätzlich wird der Inhalt im Inventar **festgehalten** — du kannst ihn nicht herausziehen (abschaltbar, siehe `preventTakingFromLockedSlots`).

*Gut für:* „Auf Platz 9 liegt immer mein Totem."

### 🔵 Item-Sperre (Taste `K`)

Sperrt einen **Item-Typ** anhand seiner Registry-ID, z. B. `minecraft:elytra`. Der Schutz gilt für jedes Exemplar, überall — Hotbar, Inventar, Kiste, egal.

*Gut für:* „Elytren werfe ich niemals weg."

### Der Unterschied in einem Satz

> Die Slot-Sperre schützt einen **Platz**, die Item-Sperre schützt eine **Sache**.

Beide zusammen sind erlaubt und schließen sich nicht aus.

---

## Was genau geschützt ist

| Weg, ein Item loszuwerden | 🔴 Slot-Sperre | 🔵 Item-Sperre |
| --- | --- | --- |
| `Q` aus der Hand | 5× nötig | 5× nötig |
| `Strg+Q` (ganzer Stapel) | 5× nötig | 5× nötig |
| `Q` im offenen Inventar | 5× nötig | 5× nötig |
| Mit der Maus aufnehmen, neben das Fenster klicken | Aufnehmen blockiert | 5× nötig |
| Droppen aus dem **Kreativ**-Inventar | — | 5× nötig |
| An einen **Rüstungsständer** anlegen | hart blockiert | hart blockiert |
| In einen **Deko-Topf** stecken | hart blockiert | hart blockiert |
| In die **Zweithand** tauschen (Taste oder Inventar) | hart blockiert | hart blockiert |
| Shift-Klick / Zahlentaste aus dem Slot heraus | blockiert | — |

Ein paar Erläuterungen dazu:

- **Warum am Rüstungsständer hart blockiert?** Weil ein einziger Rechtsklick genügt und das Item hängt am Ständer — es gibt keinen sinnvollen Grund, das fünfmal zu wollen. Mit **leerer Hand** kannst du weiterhin ganz normal Sachen vom Ständer abnehmen.
- **Warum greift die Slot-Sperre im Kreativ-Inventar nicht?** Dort ist beim Droppen nicht mehr feststellbar, aus welchem Slot das Item kam. Die Item-Sperre greift dort vollständig.
- **Shift-Klick ist kein Wegwerfen**, sondern Umsortieren — deshalb greift dort nur die Slot-Sperre.

### Bekannte Lücken

Ehrlichkeit statt Versprechen — diese Wege sind **nicht** abgedeckt:

- Ein Item im Kreativmodus zum Löschen zurück in die Item-Liste ziehen
- Tod und Despawn des Inventars
- Alles, was der Server ohne dein Zutun macht (z. B. `/clear`)
- Andere Mods, die Items direkt aus dem Inventar entfernen

---

## Das Config-Menü

Drei Wege dorthin:

| Weg | Wie |
| --- | --- |
| **Mod Menu** | Mod-Liste → ItemLocker → Zahnrad |
| **Befehl** | `/itemlocker config` |
| **Taste** | „ItemLocker-Menü öffnen" in den Steuerungs-Einstellungen belegen |

Mod Menu ist optional — ohne funktionieren Befehl und Taste trotzdem.

![Config-Menü](docs/config-screen.png)

### Item-Auswahl

![Item-Auswahl](docs/item-screen.png)

Die durchsuchbare Liste aller Items im Spiel. Tippen (`elytra`), Zeile anklicken, gesperrt. Gesperrte stehen oben und sind rot markiert.

Der Filter oben rechts hat drei Stufen:

| Filter | Zeigt |
| --- | --- |
| **Alle Items** | jedes Item im Spiel |
| **Nur gesperrte** | deine aktuelle Liste — gut zum Aufräumen |
| **Aus Inventar** | nur, was du gerade dabeihast — der schnellste Weg |

### Hotbar-Slots

![Hotbar-Slots](docs/slot-screen.png)

Die neun Plätze als anklickbare Reihe, samt Vorschau, was gerade drinliegt. Klick sperrt, Klick entsperrt.

---

## Tasten

ItemLocker bringt vier Tastenbelegungen mit. Du findest sie unter
**Optionen → Steuerung → Tastenbelegung**, in der Kategorie **Inventar**.

| Standard | Name in den Einstellungen | Was passiert |
| --- | --- | --- |
| `L` | Hotbar-Slot sperren/entsperren | Sperrt den Platz, auf dem du gerade stehst — also den aktiven Hotbar-Slot. Nochmal drücken gibt ihn wieder frei. |
| `K` | Item in der Hand sperren/entsperren | Sperrt den **Typ** des Items in deiner Hand, z. B. jede Elytra. Nochmal drücken hebt es auf. |
| *nicht belegt* | ItemLocker an/aus | Schaltet die ganze Mod um. Praktisch, wenn du kurz aufräumen willst, ohne alle Sperren zu löschen. |
| *nicht belegt* | ItemLocker-Menü öffnen | Öffnet das Config-Menü direkt, ohne Umweg über Chat oder Mod Menu. |

### Die zwei freien Tasten belegen

Die letzten beiden sind absichtlich **unbelegt**, damit ItemLocker dir keine Taste wegnimmt, die du schon benutzt. So belegst du sie:

1. `Esc` → **Optionen** → **Steuerung** → **Tastenbelegung**
2. Runterscrollen bis **Inventar**
3. Auf das Feld neben der gewünschten Zeile klicken
4. Gewünschte Taste drücken — fertig

Falls Minecraft einen Konflikt mit einer anderen Belegung anzeigt (kleines Warndreieck), such dir eine andere Taste; sonst lösen beide gleichzeitig aus.

### Gut zu wissen

- **`L` und `K` lassen sich ändern** wie jede andere Belegung — die Standardwerte sind nur ein Vorschlag.
- Die Tasten wirken **nur im Spiel**, nicht in offenen Menüs oder im Chat.
- `K` braucht ein Item in der Hand. Mit leerer Hand kommt der Hinweis *„Du hältst kein Item in der Hand"*.
- Alles, was die Tasten tun, geht auch per Befehl — siehe unten.

---

## Befehle

`/itemlocker`, kurz `/il`. Läuft komplett clientseitig — der Server sieht die Befehle nicht.

| Befehl | Beschreibung |
| --- | --- |
| `/itemlocker` | Status anzeigen |
| `/itemlocker help` | Alle Befehle |
| `/itemlocker config` | Config-Menü öffnen |
| `/itemlocker slot <1-9>` | Hotbar-Slot umschalten |
| `/itemlocker slot <1-9> lock\|unlock` | Slot gezielt sperren / freigeben |
| `/itemlocker item` | Item in der Hand umschalten |
| `/itemlocker item lock\|unlock` | Item in der Hand gezielt setzen |
| `/itemlocker item <id>` | Per Name, z. B. `minecraft:totem_of_undying` |
| `/itemlocker count <1-64>` | Wie oft gedroppt werden muss |
| `/itemlocker timeout <1-60>` | Nach wie vielen Sekunden der Zähler zurückgesetzt wird |
| `/itemlocker list` | Alle Sperren anzeigen |
| `/itemlocker clear` | Alle Sperren löschen |
| `/itemlocker on` / `off` | Mod an- / ausschalten |
| `/itemlocker hud <true\|false>` | Schloss-Symbole im HUD |
| `/itemlocker sound <true\|false>` | Warn-Sound |
| `/itemlocker messages <true\|false>` | Text über der Hotbar |
| `/itemlocker inventory <true\|false>` | Schutz in Inventar-Bildschirmen |
| `/itemlocker freeze <true\|false>` | Gesperrte Slots festhalten |
| `/itemlocker armorstands <true\|false>` | Schutz vor Rüstungsständern |
| `/itemlocker pots <true\|false>` | Schutz vor Deko-Töpfen |
| `/itemlocker offhand <true\|false>` | Zweithand-Schutz |
| `/itemlocker block` | Block, den du ansiehst, sperren / freigeben |
| `/itemlocker block <id>` | Block per Name, z. B. `minecraft:ender_chest` |
| `/itemlocker sneakbypass <true\|false>` | Ob Schleichen die Block-Sperre umgeht |

Bei `/itemlocker item <id>` schlägt die Tab-Vervollständigung alle Item-IDs vor.

---

## Die Anzeige über der Hotbar

Die Schlösser erscheinen über der Hotbar **und im offenen Inventar**:

![Schlösser im Inventar](docs/inventory-locks.png)

- **Rotes Schloss** auf einem Slot → Slot-Sperre
- **Blaues Schloss** → das Item darin ist als Typ gesperrt
- **Oranger Balken** unter dem Slot → so weit bist du beim Drop-Zähler
- **Text über der Hotbar** bei jedem geblockten Versuch, plus ein kurzer Klick-Sound, dessen Tonhöhe mit jedem Versuch steigt

Schloss, Sound und Text lassen sich einzeln abschalten.

---

## Die Config-Datei

Liegt unter `config/itemlocker.json` und wird beim ersten Start angelegt. Änderungen im Menü landen sofort darin.

```json
{
  "configVersion": 1,
  "enabled": true,
  "requiredDrops": 5,
  "resetAfterMillis": 3000,
  "guardInventoryScreens": true,
  "preventTakingFromLockedSlots": true,
  "protectArmorStands": true,
  "showHudIcons": true,
  "playSound": true,
  "actionBarMessages": true,
  "lockedSlots": [0, 8],
  "lockedItems": ["minecraft:elytra"]
}
```

| Feld | Standard | Bedeutung |
| --- | --- | --- |
| `configVersion` | `1` | Interne Formatversion. Nicht von Hand ändern. |
| `enabled` | `true` | Hauptschalter. Auf `false` verhält sich alles wie Vanilla. |
| `requiredDrops` | `5` | Wie oft gedrückt werden muss (1–64). |
| `resetAfterMillis` | `3000` | Zeitfenster in Millisekunden, siehe unten. |
| `guardInventoryScreens` | `true` | Schutz auch in Inventar- und Kisten-Bildschirmen. |
| `preventTakingFromLockedSlots` | `true` | Hält den Inhalt gesperrter Slots im Inventar fest. |
| `protectArmorStands` | `true` | Verhindert das Anlegen an Rüstungsständer. |
| `protectDecoratedPots` | `true` | Verhindert das Einfüllen in Deko-Töpfe. |
| `preventOffhandSwap` | `true` | Verhindert den Tausch in die Zweithand. |
| `lockedBlocks` | `[]` | Blöcke, deren Oberfläche gesperrt ist, als Registry-ID. |
| `blockGuiSneakBypass` | `true` | Schleichen öffnet gesperrte Blöcke trotzdem. |
| `showHudIcons` | `true` | Schlösser und Fortschrittsbalken zeichnen. |
| `playSound` | `true` | Warn-Sound bei geblocktem Versuch. |
| `actionBarMessages` | `true` | Text über der Hotbar. |
| `lockedSlots` | `[]` | Gesperrte Hotbar-Slots, **0-basiert** (0 = Taste 1). |
| `lockedItems` | `[]` | Gesperrte Item-Typen als Registry-ID. |

### Zwei Felder, die Erklärung verdienen

**`resetAfterMillis`** — Wer drei Sekunden lang nicht droppt, fängt beim nächsten Versuch wieder bei 1 an. Ohne dieses Zeitfenster würden vier Fehlgriffe über den ganzen Abend verteilt zusammenzählen, und der fünfte am nächsten Tag würde das Item fallen lassen. Genau das soll nicht passieren.

**`preventTakingFromLockedSlots`** — Ohne diese Option lässt sich ein Item aus dem gesperrten Slot mit der Maus aufnehmen. Danach liegt es **in keinem Slot mehr**, die Slot-Sperre kann also nicht mehr greifen, und ein Klick neben das Fenster wirft es weg. Wer seine Hotbar frei umsortieren will, schaltet die Option ab — dann schützt die Slot-Sperre nur noch gegen `Q`.

---

## Wie es funktioniert

ItemLocker ist **rein clientseitig**. Es hängt sich per [Mixin](https://github.com/SpongePowered/Mixin) an fünf Stellen in den Client:

| Einhängepunkt | Fängt ab |
| --- | --- |
| `ClientPlayerEntity.dropSelectedItem` | `Q` und `Strg+Q` aus der Hand |
| `ClientPlayerInteractionManager.clickSlot` | Drops in Inventar-Bildschirmen |
| `CreativeInventoryScreen.onMouseClick` | Drops aus dem Kreativ-Inventar |
| `ClientPlayerInteractionManager.interactEntityAtLocation` | Rüstungsständer |
| `ClientPlayerInteractionManager.interactEntity` | Rüstungsständer (Rückfallweg) |
| `ClientPlayerInteractionManager.interactBlock` | Deko-Töpfe und gesperrte Blöcke |
| `ClientCommonNetworkHandler.sendPacket` | Tausch in die Zweithand |
| `HandledScreen.drawSlot` | Schloss-Symbole im Inventar |

Entscheidend: Der Eingriff sitzt **vor** dem Netzwerkpaket und **vor** der lokalen Inventar-Änderung. Ein geblockter Drop erzeugt gar kein Paket — der Server erfährt nichts davon, und es kann kein Desync entstehen.

Das heißt auch: **Die Mod funktioniert auf jedem Server**, auch auf Vanilla-Servern und solchen, die keine Mods erlauben. Sie ändert nichts am Spielgeschehen, sie verwirft nur deine eigenen versehentlichen Eingaben. Ein Vorteil verschafft sie dir nicht.

---

## Kompatibilität

| | Status |
| --- | --- |
| Minecraft | 26.2 |
| Fabric Loader | 0.16.0 oder neuer |
| Fabric API | erforderlich |
| Java | 25 oder neuer |
| Mod Menu | optional |
| Server | jeder — die Mod läuft nur bei dir |
| Andere Mods | keine bekannten Konflikte |

Für eine andere Minecraft-Version passt du `minecraft_version`, `yarn_mappings` und `fabric_version` in [`gradle.properties`](gradle.properties) an und baust neu. Die passenden Werte findest du auf [fabricmc.net/develop](https://fabricmc.net/develop).

---

## Wenn nichts passiert

Verhält sich das Droppen weiter wie in Vanilla, geh der Reihe nach vor:

**1. Liegt Fabric API im `mods`-Ordner?** Das ist mit Abstand die häufigste Ursache, gerade auf Lunar Client.

**2. Startet die Mod überhaupt?** In `logs/latest.log` muss stehen:

```
[ItemLocker] ItemLocker bereit - 0 gesperrte Slots, 1 gesperrte Items, 5 Drops noetig
```

**3. Greifen die Einhängepunkte?** Starte Minecraft einmal mit diesem JVM-Argument:

```
-Ditemlocker.selftest=true
```

Dann prüft die Mod beim Start jeden einzelnen Einhängepunkt und schreibt pro Stelle eine Zeile ins Log:

```
Selbsttest OK: ...ClientPlayerEntity enthaelt handler$...$itemlocker$guardHotbarDrop
```

Steht dort stattdessen `Selbsttest FEHLGESCHLAGEN`, wurden die Mixins nicht angewendet — meist wegen einer falschen Minecraft-Version oder eines Launchers, der Mixins nicht lädt.

**4. Ist die Mod eingeschaltet?** `/itemlocker` zeigt den Status. Auch `guardInventoryScreens` prüfen, wenn es nur im Inventar hakt.

---

## Selbst bauen

Du brauchst **JDK 25**. Fehlt es, lädt Gradle es automatisch nach.

```bash
./gradlew build
```

Das fertige Jar liegt unter `build/libs/itemlocker-2.0.1+26.2.jar` — der Teil hinter dem Plus ist die Minecraft-Version. Die Datei mit `-sources` im Namen wird zum Spielen nicht gebraucht.

### Ein Handgriff vor dem ersten `runClient`

Leg eine **unveränderte Fabric-API-Jar für 26.2** in den Ordner `run/mods`. Ohne sie startet der Entwicklungs-Client nicht.

Der Grund: Ab 26.x ist Minecraft unverschleiert, es gibt also keine Übersetzungsebene mehr. Loom schreibt eingebundene Mods trotzdem auf den Namensraum `named` um — den der Loader dort nicht kennt, weil er auf `official` läuft. Er bricht dann beim Lesen der Class-Tweaker ab:

```
Namespace (named) does not match current runtime namespace (official)
```

Deshalb sind Fabric API und Mod Menu in [`build.gradle`](build.gradle) nur mit `modCompileOnly` eingebunden — zur Laufzeit kommt die echte Jar aus `run/mods`, die bereits in `official` vorliegt.

Entwicklungs-Client mit eingebautem Selbsttest starten:

```bash
./gradlew runClient
```

### Aufbau des Projekts

```
src/main/java/com/itemlocker/
├── ItemLocker.java              Konstanten und Logger
├── client/
│   ├── ItemLockerClient.java    Einstiegspunkt
│   ├── ItemLockerKeybinds.java  Tasten
│   ├── ItemLockerCommands.java  Befehle
│   ├── LockHudElement.java      Schlösser über der Hotbar
│   ├── MixinSelfTest.java       Selbstprüfung der Einhängepunkte
│   └── screen/                  Config-Menü, Item- und Slot-Auswahl
├── compat/ModMenuIntegration    Zahnrad-Knopf in Mod Menu
├── config/                      JSON-Config und Migration
├── lock/
│   ├── LockManager.java         Was ist gesperrt?
│   ├── DropAttemptTracker.java  Der Zähler
│   ├── DropGuard.java           Entscheidung bei Drops
│   ├── PlacementGuard.java      Entscheidung bei Rüstungsständern
│   └── Feedback.java            Text und Sound
└── mixin/                       Die Einhängepunkte
```

---

## Auf GitHub veröffentlichen

### Bereits eingerichtet

- `github_user=Sercigamer` in [`gradle.properties`](gradle.properties) — daraus baut Gradle den Autor-Eintrag und die Repo-Links in `fabric.mod.json`
- Commit-Identität auf die GitHub-noreply-Adresse, damit keine private E-Mail in der Historie landet

### Pushen

```bash
git push -u origin main
```

### Release veröffentlichen

Trag die Änderungen in [`CHANGELOG.md`](CHANGELOG.md) unter einer Überschrift `## [1.3.0]` ein, setze `mod_version` in `gradle.properties` auf dieselbe Nummer, dann:

```bash
git tag v1.3.0 && git push origin v1.3.0
```

Der Workflow prüft, dass Tag und `mod_version` übereinstimmen, baut die Mod, **zieht den Release-Text aus dem passenden CHANGELOG-Abschnitt** und hängt Installationsanleitung und Schnellstart an. Ans Release kommt nur das Mod-Jar — das Sources-Jar wird aussortiert, damit es niemand versehentlich in den `mods`-Ordner legt.

### Was schon eingerichtet ist

- `.github/workflows/build.yml` — baut bei jedem Push, Jar als Artifact
- `.github/workflows/release.yml` — Release bei Tag `v*`, mit Platzhalter- und Versionsprüfung
- `.gitignore` — blockt `build/`, `run/`, IDE-Dateien und gängige Zugangsdaten-Dateien
- `LICENSE` (MIT), `CHANGELOG.md`, `.gitattributes`

---

## Lizenz

MIT — siehe [LICENSE](LICENSE).
