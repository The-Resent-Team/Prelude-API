# Client Side Implementation Specs

# THIS IS OUTDATED WITH THE NEW PROTOCOL
### will update soon

## General
- All packets are sent over the PluginMessage channels
- All channels use the `resent:prelude` namespace
<details>
<summary><strong>This information is not needed when working with MCP</strong></summary>
- Because we use the PluginChannel, all packets are `Play` packets
</details>

## Mod Details
- If the server sends a `disable` packet you are required to respect that and forcibly disable the mod until the user disconnects from the server
- The `init` packet is not a required to be handled, it is used to signify to the client that the server has a hook for that mod. It is sent on the server side join event.
- Rarely a mod will send an `init` and a `disable` packet, in this case the mod must respect the `disable` packet

## Mod Specific Details
In this section you will find details that are specific to each mod

### OffHand (Receiver ID: `off_hand`)
<details>
<summary><strong>Caveat with the server side implementation</strong></summary>
There are some caveats with this mod, when the server is using the base implementation this packet will only get sent every `0.5s` (`10 ticks`).<br/>
The reason for this is that there is no singular guaranteed method to check if the offhand item has been changed.<br/>
So we run a task timer to see if the value has changed from last iteration, this also (marginally) reduces memory usage client side.<br/>
You only have to construct an item stack with the material type and enchant it with a random enchant to display as those are the only things that matter to display the item
</details>

- The offhand mod requires each packet's message to contain more detail so the message field is formatted with JSON instead of a raw string
- The offhand mod can send 2 additional packets on top of the base packets
- - The first packet is an Equip packet, the message is formatted as follows `{"action":"equip_item","item_id":"%item_id%","enchanted":"%enchanted%"}` where `%item_id%` is replaced with the items enum name as defined in the Bukkit API and `%enchanted%` is replaced with a boolean depending on whether the item has enchants or not 
- - The second packet is an Un-Equip packet, the message is formatted as follows `{"action":"un-equip_item","item_id":"AIR","enchanted":"false"}`

### TotemTweaks (Mod ID: `totem_tweaks`)
It was requested that it would fire only if the totem was in the secondary hand but there is no way of checking this, if you find a way feel free to make a PR.

- The totem tweaks mod sends the `totem_consumed` packet when the server calls the `EntityResurrectEvent`
- Depending on the implementation there is a rare chance that the packet is sent even if the `EntityResurrectEvent` is cancelled, this is plugin compatability issue, this is not the fault of us or you, the base implementation ignores cancelled events and runs last in the handlers chain

### ServerTps (Mod ID: `server_tps`)
- The message of this mod contains a raw string of a double
- Should send every second, may be adjusted at will by the server side implementation though

### FreeLook (Mod ID: `free_look`)
- This mod has no hooks, but you must respect the `disable` packet

### AnchorRenderer (Mod ID: `anchor_renderer`)
- The anchor renderer mod requires each packet's message to contain more detail so the message field is formatted with JSON instead of a raw string
- This mod will send a packet whenever an anchor is updated, the message is formatted as follows
`{"x":"%x%","y":"%y%","z":"%z%","charge":"%charge%"}` where `%x%`, `%y%` and `%z%` are replaced with the location of the update and `%charge%` is replaced with an integer of 1-3 depending on how many charges the anchor has. Charge of 0 represent an anchor getting placed, while a charge of -1 indicates an anchor explodes