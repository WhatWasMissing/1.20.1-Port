# AE2 + GuideME integration

## Target versions

- Applied Energistics 2 Forge 15.4.10
- GuideME 20.1.15

Both integrations are optional. Matter Overdrive must remain loadable and playable with either or both mods absent.

## Legacy source audit

The authoritative Matter Overdrive 1.7.10 and 1.12.2 JARs contain no direct `appeng` / AE2 API classes or hard dependency. Their compatibility model is therefore treated as interoperability through inventories and machine/network boundaries, not as a merged ME/Matter Overdrive network implementation.

## AE2

Matter Overdrive exposes Forge `IItemHandler`, `IEnergyStorage`, and its own matter capability where appropriate. AE2 15.4.10 can therefore automate public item inventories using Storage Buses / import-export style capability access without a hard Matter Overdrive -> AE2 API dependency.

Audited examples:

- Tritanium Crates expose their 54-slot item handler.
- Decomposer exposes its item handler, FE storage and matter storage.
- Matter Recycler exposes its item handler and FE storage.
- Replicator exposes its item handler, FE storage and matter storage; its Matter Overdrive network task queue remains internal and is not replaced by AE2.
- Pattern Storage exposes its energy/battery + Pattern Drive inventory and FE storage.
- Inscriber exposes its processing inventory and FE storage.

Upgrade inventories are separate from these public handlers and are not exposed through the normal machine item capability.

AE2 and the Matter Overdrive Router/Switch network remain separate systems. They can meet at capability-exposed inventories. No implicit AE <-> FE conversion is added.

## GuideME

When GuideME 20.1.x is installed, Matter Overdrive registers `matteroverdrive:guide`. GuideME's default resource lookup maps that guide to:

`assets/matteroverdrive/guides/matteroverdrive/guide`

The Data Pad keeps scan history and contract controls. A `GuideME Manual` button opens the rich manual when GuideME is installed. The compact built-in Data Pad pages remain the fallback when GuideME is absent.

Current GuideME sections cover survival progression, matter technology, power/machines, Androids, fusion reactor, weapons, Matter Network, AE2, quests/contracts, Dimensional Pylon, and transporter/security.
