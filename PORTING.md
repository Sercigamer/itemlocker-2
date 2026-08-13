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
| Kreativ-Drop | `CreativeInventoryScreen.onMouseClick` | `AbstractContainerScreen.slotClicked(Slot,int,int,ContainerInput)` bzw. `MultiPlayerGameMode.handleCreativeModeItemDrop(ItemStack)` |
| Zweithand | `ClientCommonNetworkHandler.sendPacket` | vermutlich unverändert, noch zu prüfen |

## Der eigentliche Aufwand: die Oberfläche

Die Schutzlogik ist reine Umbenennungsarbeit. **Die Render-Schicht ist es
nicht.** In 26.x wurde die GUI-Ausgabe grundlegend umgebaut:

- `GuiGraphics` (bei uns `DrawContext`) existiert nicht mehr
- Stattdessen `GuiGraphicsExtractor` mit einem Extraktions-Schritt vor dem
  Zeichnen: `AbstractContainerScreen.extractSlots(GuiGraphicsExtractor,int,int)`
- Ein `drawSlot`/`renderSlot` gibt es nicht mehr — der Einhängepunkt für die
  Schloss-Symbole im Inventar fällt damit weg
- Die Fabric-HUD-API dürfte entsprechend anders aussehen

Betroffen sind: `LockIcon`, `LockHudElement`, `HandledScreenMixin` und alle
vier Config-Bildschirme.

## Vorschlag für die Reihenfolge

1. **Schutzlogik portieren** — Mixins, Guards, Config, Befehle, Tasten.
   Das ist der Kern und reine Umbenennung.
2. **Ohne Oberfläche veröffentlichen** — Bedienung über `/itemlocker` und
   die Tasten. Damit ist 26.x nutzbar.
3. **Oberfläche nachziehen**, sobald die neue Render-API verstanden ist.
