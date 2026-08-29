# Machine debug progress

- Added an opt-in **INF FE** button to the reactor, decomposer, inscriber, matter analyzer, matter recycler, replicator, solar panel, and transporter GUIs.
- The server handles the toggle through the standard container-button callback.
- The setting is saved in the owning block entity as `InfiniteEnergy`.
- When enabled, the machine reports a full configured buffer and extraction requests do not reduce it. Disabled machines retain normal energy behavior.
- Each toggle confirms through the action bar: `[DEBUG] Infinite energy: ON/OFF`.

## Test

1. Open each listed GUI and click **INF FE**; confirm the action-bar state.
2. With ON, run the machine without a cable or battery input and verify progress continues.
3. Break/reload the machine and confirm the setting remains enabled.
4. Toggle OFF and confirm normal energy is required again.

Next: functional tritanium tools and armour.
