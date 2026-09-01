# Matter Network Logistics Test Checklist

Branch: `feature/android-system-weapon-hud`

The existing Pattern Network remains the data path for Analyzer, Pattern Storage, Pattern Monitor, and Replicator. This pass adds the general item logistics layer below.

## Router and Network Pipe

1. Place a **Network Router** and power it from Reactor IO / a heavy energy cable.
2. Connect one chest to the Router through **Network Pipe**.
3. Connect a second chest on the same Network Pipe graph.
4. Put items into the first chest.

Expected:

- The Router GUI reports stored FE, endpoint count, network-node count, Pylon count, and items moved on the previous tick.
- Items move from a source chest to an available destination chest.
- The Router consumes 10 FE per item moved and stops when unpowered.
- Put one item into the Router's filter slot. It becomes a whitelist: only matching item stacks may be moved.
- Remove the filter to route any item again.

## Network Switch

1. Place a **Network Switch** in the only Network Pipe route between Router and a chest.
2. Right-click it once.

Expected:

- It reports `DISABLED`.
- The Router endpoint count drops and no items cross that point.
- Right-click again; it reports `ENABLED`, endpoint count returns, and routing resumes.
- Save/reload and confirm the enabled state persists.

## Pylon link

1. Build two separate Router/Network Pipe graphs, each with a Pylon.
2. Keep pylons within 64 blocks.
3. Shift-right-click each Pylon until both report the same channel (0-15).
4. Power the Router and add a source chest to one graph and destination chest to the other.

Expected:

- The Router GUI reports at least one Pylon.
- Matching channel Pylons bridge the two networks wirelessly and items transfer.
- Change one Pylon to a different channel: routing stops.
- Restore the channel: routing resumes.

## Regression

- Existing Pattern Storage/Monitor/Replicator networking still discovers patterns through Network Pipe.
- Matter Pipe and Heavy Matter Pipe still transport matter independently of this item network.
- Save/reload with Router filter, Switch state, and Pylon channel set; all persist.
