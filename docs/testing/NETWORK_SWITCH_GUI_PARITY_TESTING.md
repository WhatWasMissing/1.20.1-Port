# Network Switch GUI Parity Testing

Target branch: `testing/main`
Build identity: Alpha Version 3
Legacy references: Matter Overdrive 1.7.10 0.4.2 and 1.12.2 0.7.1.0 only.

## Restored in this pass

- Network Switch now has a real 1.20.1 menu/screen instead of only toggling from block interaction.
- The authoritative 1.12 `GuiNetworkSwitch` included an `ElementConnections` region. That legacy element's methods are empty, so this port does not imitate a non-functional widget; it exposes real live connection state from the current routing backend instead.
- Six-sided adjacency telemetry reports actual directly traversable network transport blocks or exposed item/matter endpoints.
- The persisted switch enabled/disabled state is synchronized into the GUI.
- The GUI toggle is a real server-side menu button that changes the same backend state used by item/matter routing.
- Normal right-click opens the operator GUI. Shift-right-click retains the port's existing quick-toggle behavior as modern QoL.
- The switch still has no invented machine inventory. Player inventory/hotbar remain available only for normal inventory management.

## Runtime checklist

1. Place a Network Switch and right-click it. Confirm the operator GUI opens.
2. Confirm STATUS begins ONLINE for a fresh switch and the block's existing active appearance remains consistent.
3. Press DISABLE. Confirm STATUS changes to OFFLINE and routing through the switch stops.
4. Press ENABLE. Confirm routing resumes without breaking/replacing blocks.
5. Close/reopen the GUI. Confirm enabled state persists.
6. Save/reload the world. Confirm enabled state persists and GUI status matches routing state.
7. Shift-right-click the block. Confirm it toggles immediately without opening the GUI.
8. Attach a Network Pipe on each horizontal side one at a time. Confirm N/S/E/W indicators update to match the real adjacent side.
9. Attach traversable connections above/below where practical. Confirm U/D indicators update.
10. Attach an item-handler machine directly to the switch. Confirm that side reports connected when the current item graph can use it.
11. Attach a matter-capability machine directly to the switch. Confirm that side reports connected when the current matter graph can use it.
12. Remove each neighbor and confirm the corresponding link indicator clears without reopening the GUI.
13. Connect Router, Pylon, another Network Switch and Matter/Network Pipes and confirm each valid transport is represented.
14. Disable an adjacent second Network Switch. The local physical adjacency indicator may remain present, but end-to-end routing must obey the disabled neighbor's backend state.
15. Shift-click between the player's main inventory and hotbar while this GUI is open. Confirm normal player-only movement works and no phantom switch inventory exists.
16. Multiplayer: have one player toggle the GUI while another has it open. Confirm synchronized enabled state updates without desync.

## Visual checks for later

- 230x190 frame fits at normal/reduced GUI scales.
- Six connection labels/lines remain readable.
- Toggle button does not overlap status text.
- Player inventory and hotbar line up with their actual slots.
