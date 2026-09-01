# Matter Network Logistics Test Checklist

Branch: `testing/main`

The existing Pattern Network remains the data path for Analyzer, Pattern Storage, Pattern Monitor, and Replicator. This pass also covers the general item logistics layer.

## Router and Network Pipe

1. Place a **Network Router** and power it from Reactor IO / a Heavy Energy Cable.
2. Connect one chest to the Router through **Network Pipe**.
3. Connect a second chest on the same Network Pipe graph.
4. Put items into the first chest.

Expected:

- The Router GUI reports stored FE, endpoint count, network-node count, Pylon count, and items moved on the previous tick.
- Items move from the source chest to an available destination chest.
- Once an endpoint receives routed items it is treated as a sink until emptied, so the same stack must not bounce A -> B -> A every tick.
- The Router consumes 10 FE per item actually moved and stops when unpowered.
- Put one item into the Router's filter slot. It becomes a whitelist: only matching item stacks may be moved.
- Remove the filter to route any item again.
- Break a Router while its filter slot is occupied. Confirm the filter item drops instead of being deleted.

## Multiple routers

1. Put two powered Routers on the same connected item network.
2. Give the network one source chest and one destination chest.

Expected:

- Only one Router executes movement for the shared network on a tick.
- Items are not duplicated, double-moved, or charged twice because two Routers can see the same endpoints.
- If the routing Router loses power, another powered Router on the graph can take over.

## Network Switch

1. Place a **Network Switch** in the only Network Pipe route between Router and a chest.
2. Right-click it once.

Expected:

- It reports `DISABLED`.
- The Router endpoint count drops and no item logistics cross that point.
- Pattern Storage/Monitor/Replicator discovery also stops across the disabled Switch.
- Right-click again; it reports `ENABLED`, endpoint/pattern discovery returns, and routing resumes.
- Save/reload and confirm the enabled state persists.

## Pylon link

1. Build two separate Router/Network Pipe graphs, each with a Pylon.
2. Keep pylons within 64 blocks.
3. Shift-right-click each Pylon until both report the same channel (0-15).
4. Power the Router network and add a source chest to one graph and destination chest to the other.

Expected:

- The Router GUI reports at least one Pylon.
- Matching channel Pylons bridge the two networks wirelessly and items transfer.
- The two Router graphs do not fight over the same inventory or bounce transferred items back across the Pylon link.
- Change one Pylon to a different channel: routing stops.
- Restore the channel: routing resumes.

## Regression

- Existing Pattern Storage/Monitor/Replicator networking still discovers patterns through Network Pipe.
- Matter Pipe transports matter independently of the item network.
- Heavy Energy Cable carries FE only and must not become a valid matter-routing path.
- Save/reload with Router filter, learned sink state, Switch state, and Pylon channel set; all persist as appropriate.
