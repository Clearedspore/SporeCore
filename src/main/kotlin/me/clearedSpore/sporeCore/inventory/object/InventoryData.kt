package me.clearedSpore.sporeCore.inventory.`object`

import me.clearedSpore.sporeAPI.util.ItemUtil
import me.clearedSpore.sporeAPI.util.time.TimeUtil
import me.clearedSpore.sporeCore.util.doc.DocReader
import me.clearedSpore.sporeCore.util.doc.DocWriter
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.dizitart.no2.collection.Document


data class InventoryData(
    val id: String,
    val owner: String,
    val contents: List<ItemStack?>,
    val armor: List<ItemStack?>,
    val offhand: ItemStack?,
    val timestamp: Long,
    val saveLocation: Location? = null,
    val experience: Int = 0,
    val storeReason: String = "",
    var rollbackIssuer: String = "",
    var messageID: String = ""
) {

    fun toDocument(): Document = DocWriter()
        .put("id", id)
        .put("owner", owner)
        .put("contents", contents.map { ItemUtil.itemStackToBase64(it) })
        .put("armor", armor.map { ItemUtil.itemStackToBase64(it) })
        .put("offhand", ItemUtil.itemStackToBase64(offhand))
        .put("timestamp", timestamp)
        .putLocation("saveLocation", saveLocation)
        .put("experience", experience)
        .put("storeReason", storeReason)
        .put("rollbackIssuer", rollbackIssuer)
        .put("messageID", messageID)
        .build()

    companion object {

        fun fromPlayer(
            player: Player,
            id: String,
            saveLocation: Location? = null,
            storeReason: String = ""
        ): InventoryData {
            val inv = player.inventory
            return InventoryData(
                id = id,
                owner = player.uniqueId.toString(),
                contents = inv.contents.toList(),
                armor = inv.armorContents.toList(),
                offhand = inv.itemInOffHand,
                timestamp = System.currentTimeMillis(),
                saveLocation = saveLocation?.rounded(),
                experience = player.level,
                storeReason = storeReason,
                rollbackIssuer = "",
                messageID = ""
            )
        }


        fun fromDocument(doc: Document): InventoryData {
            val reader = DocReader(doc)

            val id = reader.string("id") ?: throw IllegalArgumentException("Invalid inventory document")
            val owner = reader.string("owner") ?: throw IllegalArgumentException("Invalid inventory document")

            val contents = reader.list("contents").mapNotNull { ItemUtil.itemStackFromBase64(it as? String) }
            val armor = reader.list("armor").mapNotNull { ItemUtil.itemStackFromBase64(it as? String) }
            val offhand = ItemUtil.itemStackFromBase64(doc["offhand"] as? String)

            val timestamp = reader.long("timestamp")
            val saveLocation = reader.location("saveLocation")
            val experience = reader.int("experience")
            val storeReason = reader.string("storeReason") ?: ""

            val rollbackIssuer = reader.string("rollbackIssuer") ?: ""
            val messageID = reader.string("messageID") ?: ""

            return InventoryData(
                id = id,
                owner = owner,
                contents = contents,
                armor = armor,
                offhand = offhand,
                timestamp = timestamp,
                saveLocation = saveLocation,
                experience = experience,
                storeReason = storeReason,
                rollbackIssuer = rollbackIssuer,
                messageID = messageID
            )
        }
    }


    fun formattedLocation(): String {
        val loc = saveLocation ?: return "No location saved"
        val world = loc.world?.name ?: "Unknown"
        return "World: $world | X: ${loc.blockX} | Y: ${loc.blockY} | Z: ${loc.blockZ}"
    }

    fun formattedAge(): String {
        val elapsed = System.currentTimeMillis() - timestamp
        return TimeUtil.formatDuration(elapsed)
    }

}

private fun Location.rounded(): Location {
    return Location(
        this.world,
        this.blockX.toDouble(),
        this.blockY.toDouble(),
        this.blockZ.toDouble()
    )
}

