# Portierung auf Minecraft 26.x

Arbeitsnotizen für den Zweig `mc/26.2`. Alle Namen und Signaturen hier sind
gegen das echte Client-Jar von 26.2 geprüft, nicht geraten.

## Ausgangslage

Ab 26.x liefert Mojang das Spiel **unverschleiert** aus — 10.372 lesbare
`net/minecraft/*`-Klassen. Es gibt deshalb weder Yarn- noch Mojang-Mappings:
Es ist nichts mehr zu übersetzen.

### Was die Werkzeugkette trotzdem braucht

Loom besteht auf einem Mappings-Eintrag. Der Weg dorthin:

| Problem | Lösung |
| --- | --- |
| `net.fabricmc:intermediary:26.2` deklariert sich als Version `0.0.0` | nicht über Maven auflösbar — Datei direkt einbinden |
| Der Hülle fehlt der Namensraum `named` | eigene Identitäts-Datei, tiny v1, drei gleichnamige Namensräume |
| Windows-PowerShell packt Zips mit Backslashes | mit dem `jar`-Werkzeug des JDK packen, sonst findet Loom `mappings/mappings.tiny` nicht |
| Loom < 1.17.13 lehnt Mod Menu 20.0.0 ab | Loom 1.17.19 |

Dazu: **Java 25** ist Pflicht, Fabric API `0.157.0+26.2`, Mod Menu `20.0.0`.

## Geprüfte Namensübersetzung

| 1.21.11 (Yarn) | 26.2 (Mojang) |
| --- | --- |
| `MinecraftClient` | `client.Minecraft` |
| `ClientPlayerEntity` | `client.player.LocalPlayer` |
| `ClientPlayerInteractionManager` | `client.multiplayer.MultiPlayerGameMode` |
| `ClientCommonNetworkHandler` | `client.multiplayer.ClientCommonPacketListenerImpl` |
| `HandledScreen` | `…gui.screens.inventory.AbstractContainerScreen` |
| `CreativeInventoryScreen` | `…inventory.CreativeModeInventoryScreen` |
| `ButtonWidget` | `…gui.components.Button` |
| `CyclingButtonWidget` | `…gui.components.CycleButton` |
| `SliderWidget` | `…gui.components.AbstractSliderButton` |
| `TextFieldWidget` | `…gui.components.EditBox` |
| `AlwaysSelectedEntryListWidget` | `…gui.components.ObjectSelectionList` |
| `KeyBinding` | `client.KeyMapping` |
| `Text` | `network.chat.Component` |
| `Formatting` | `ChatFormatting` |
| `Identifier` | `resources.Identifier` (Mojang nennt es wieder so) |
| `Registries` | `core.registries.BuiltInRegistries` |
| `PlayerEntity` | `world.entity.player.Player` |
| `PlayerInventory` | `world.entity.player.Inventory` |
| `ScreenHandler` | `world.inventory.AbstractContainerMenu` |
| `SlotActionType` | `world.inventory.ContainerInput` |
| `Hand` | `world.InteractionHand` |
| `ActionResult` | `world.InteractionResult` |
| `Vec3d` | `world.phys.Vec3` |
| `World` | `world.level.Level` |
| `ArmorStandEntity` | `world.entity.decoration.ArmorStand` |
| `PlayerActionC2SPacket` | `…protocol.game.ServerboundPlayerActionPacket` |
| `PositionedSoundInstance` | `client.resources.sounds.SimpleSoundInstance` |
| `RenderTickCounter` | `client.DeltaTracker` |
| `Click` | `client.input.MouseButtonEvent` |

Unverändert: `Screen`, `Slot`, `ItemStack`, `Block`, `Blocks`, `Items`,
`BlockState`, `BlockPos`, `Direction`, `BlockHitResult`, `EntityHitResult`,
`HitResult`, `Entity`, `DecoratedPotBlock`, `SoundEvents`, `Packet`.

## Einhängepunkte in 26.2

| Zweck | 1.21.11 | 26.2 |
| --- | --- | --- |
| Drop aus der Hand | `ClientPlayerEntity.dropSelectedItem(boolean)` | `LocalPlayer.drop(boolean)` |
| Klick im Inventar | `…InteractionManager.clickSlot(int,int,int,SlotActionType,PlayerEntity)` | `MultiPlayerGameMode.handleContainerInput(int,int,int,ContainerInput,Player)` |
| Block-Rechtsklick | `interactBlock(…)` | `MultiPlayerGameMode.useItemOn(LocalPlayer, InteractionHand, BlockHitResult)` |
| Entity-Rechtsklick | `interactEntity` **und** `interactEntityAtLocation` | **zusammengelegt** zu `interact(Player, Entity, EntityHitResult, InteractionHand)` |
| Kreativ-Drop | `CreativeInventoryScreen.onMouseClick` | `CreativeModeInventoryScreen.slotClicked(Slot,int,int,ContainerInput)` |
| Zweithand | `ClientCommonNetworkHandler.sendPacket` | `ClientCommonPacketListenerImpl.send(Packet)` |

## Die Render-Schicht

In 26.x wurde die GUI-Ausgabe umgebaut: Statt direkt zu zeichnen, füllt man
in einem Extraktions-Schritt einen `GuiGraphicsExtractor`. **Alle von uns
benötigten Bausteine existieren weiterhin**, nur unter anderen Namen — die
Übersetzung ist mechanisch.

| 1.21.11 | 26.2 |
| --- | --- |
| `Screen.render(DrawContext, mouseX, mouseY, delta)` | `Screen.extractRenderState(GuiGraphicsExtractor, mouseX, mouseY, partialTick)` |
| `AbstractWidget.renderWidget(…)` | `extractWidgetRenderState(GuiGraphicsExtractor, int, int, float)` |
| `EntryListWidget.Entry.render(DrawContext, …, hovered, delta)` | `AbstractSelectionList.Entry.extractContent(GuiGraphicsExtractor, mouseX, mouseY, hovered, partialTick)` |
| `context.fill(x1,y1,x2,y2,argb)` | `extractor.fill(x1,y1,x2,y2,argb)` — unverändert |
| `context.drawItem(stack,x,y)` | `extractor.item(stack,x,y)` |
| `context.drawTextWithShadow(font,text,x,y,color)` | `extractor.text(font, component, x, y, color, true)` |
| `context.drawCenteredTextWithShadow(…)` | `extractor.text(…)`, Zentrierung selbst rechnen |
| `TextRenderer` | `client.gui.Font` |

Für die Schlösser im Inventar gibt es kein `drawSlot` mehr. Ersatz:
`AbstractContainerScreen.extractSlots(GuiGraphicsExtractor,int,int)` am Ende
anhängen und über `menu.slots` selbst zeichnen — `Slot.x`/`Slot.y` liefern
weiterhin die Position.

Die Fabric-HUD-API (`HudElementRegistry`, `VanillaHudElements`) gibt es in 26.x unverändert.

## Gelöst: der Namensraum-Konflikt im Entwicklungs-Client

Der Dev-Client brach lange ab mit:

`Namespace (named) does not match current runtime namespace (official)`

Ursache war **nicht** die Mapping-Datei, sondern: Loom schreibt jede eingebundene Mod auf `named` um — einen Namensraum, den es ab 26.x nicht mehr gibt, weil das Spiel unverschleiert ausgeliefert wird. Der Loader läuft dort auf `official`.

**Lösung:** Fabric API und Mod Menu nur mit `modCompileOnly` einbinden; zur Laufzeit die unveränderte Jar aus `run/mods` verwenden, die bereits in `official` vorliegt.

Mod Menu war dabei der zähste Teil — es bringt fünf Fabric-Module als eingebettete Jars mit, die über denselben Weg umgeschrieben wurden. Deshalb tauchte nach jedem Versuch ein anderes Modul in der Fehlermeldung auf.

Erfolglos vorher versucht: die Mapping-Datei mit allen 10.953 Klassen füllen, auf vier Module abspecken, `fabric.development` erzwingen. Keiner dieser Wege veränderte den Namensraum.

## Stolperfalle: keine Item-Stapel außerhalb einer Welt

`new ItemStack(item)` wirft ab 26.x im Hauptmenü `NullPointerException: Components not bound yet`. Die Item- und Block-Auswahl bauten für jeden Eintrag einen Stapel — betroffen war jeder, der das Menü über Mod Menu öffnet. Die Listen arbeiten deshalb jetzt mit Registry-IDs; Name aus dem Übersetzungsschlüssel, Symbol nur wenn ein Stapel möglich ist.

## Stand

Beide Fassungen sind veröffentlicht: **2.0.0 für 26.1**, **2.0.1 für 26.2**.
Im Dev-Client geprüft — die Mod lädt, alle sieben Einhängepunkte greifen,
alle vier Bildschirme rendern, und eine Welt-Sitzung lief ohne einen
Fehler aus dem Mod-Code.

26.1 und 26.2 unterscheiden sich in genau einer Zeile: Der Text über der
Hotbar läuft in 26.1 über `Gui`, in 26.2 über `Gui.hud`. Deshalb gibt
es je Version einen eigenen Zweig.
