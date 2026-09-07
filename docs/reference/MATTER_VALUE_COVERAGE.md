# Matter Value Coverage

## Goal
Every obtainable registered item should have a positive Matter value, while craftable items should inherit the subtotal of the components used to make them.

## Resolution order
1. Dynamic values for data-bearing Matter items such as Matter Dust.
2. Explicit base values for raw/special materials and deliberately balanced items.
3. Material-tag base values for broad raw-material families such as logs, planks, leaves, wool and saplings.
4. Recursive recipe-derived values.
5. Conservative fallback valuation for items with no fully resolvable recipe path.

Explicit and tag-base values are authoritative. Recipes do not undercut an explicit base value.

## Recipe-derived values
For each recipe that creates the target item:
- Resolve every ingredient recursively.
- For tag/alternative ingredients, use the cheapest valid option.
- Sum the resolved ingredient Matter values.
- Divide the subtotal by the recipe output count.
- Round upward so multi-output conversions cannot create free Matter through fractional rounding.
- If several valid recipes produce the same item, use the cheapest complete recipe path.
- Detect cycles and cap recursion depth to prevent reversible recipes or malformed recipe graphs from looping forever.

This naturally supports multi-stage crafting. A machine inherits the Matter value of its components, and those components inherit the value of their own components until the graph reaches raw/base materials.

## Fallback values
Fallback valuation exists so uncraftable loot, special items and modded items without a resolvable recipe do not silently become zero-Matter items.

Fallbacks are intentionally conservative:
- Stackable commodity items start low.
- Low-stack-size items cost more.
- Damageable items scale with durability.
- Uncommon, Rare and Epic rarity tiers multiply the fallback value.

Fallback values are a safety net, not preferred balance data. Frequently encountered fallback items should be promoted to explicit values after runtime review.

## Player-facing inspection
All item tooltips now show the resolved Matter value. Holding Shift also shows the source:
- dynamic contents
- explicit base value
- material tag base value
- recipe component subtotal
- unresolved-item fallback

## Runtime coverage audit
When a server starts, Matter Overdrive scans the complete registered item registry and logs a summary in this form:

`MATTER VALUE AUDIT: valued=X/Y explicit=A tagBase=B recipe=C fallback=D dynamic=E unresolved=F`

It also logs a small sample of fallback-valued item IDs. This makes it straightforward to identify important items that still need deliberate balance values.

## Economy safeguards
- Analyzer, Decomposer and recorded replication patterns use the same level-aware resolver.
- Recipe output counts are accounted for.
- Alternative ingredients resolve deterministically to the cheapest valid Matter path.
- Recipe loops are detected.
- Upward rounding prevents fractional multi-output gain loops.
- Explicit values remain authoritative.
- Stored FE, Matter contents or arbitrary NBT are not added to a shell item's Matter value, preventing filled storage items from duplicating their contents through decomposition.

## Runtime tests
- [ ] Raw explicit item shows expected Matter value/source.
- [ ] Log/plank/tag material shows expected tag-base value/source.
- [ ] Simple crafted item equals component subtotal divided by output count.
- [ ] Multi-stage crafted item recursively equals the subtotal of its raw-material chain.
- [ ] Multi-output recipe rounds conservatively and cannot create a decomposition/replication profit loop.
- [ ] Reversible recipes terminate without recursion errors.
- [ ] Alternative/tag ingredient recipe resolves deterministically.
- [ ] Smelting/blasting/stonecutting recipes derive values where all ingredients resolve.
- [ ] Matter Overdrive machine/component recipes inherit their ingredients' values.
- [ ] Third-party mod recipe derives a value when its ingredients resolve.
- [ ] Uncraftable item receives a positive fallback value.
- [ ] Shift tooltip reports the correct source.
- [ ] Analyzer records the same value shown in the tooltip.
- [ ] Decomposer produces the same Matter amount shown in the tooltip.
- [ ] Replicator consumes the same value stored by the Analyzer.
- [ ] Server startup audit reports zero unresolved registered non-air items.
