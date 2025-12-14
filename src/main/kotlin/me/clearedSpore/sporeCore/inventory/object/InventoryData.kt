package me.clearedSpore.sporeCore.inventory.`object`

import me.clearedSpore.sporeAPI.util.TimeUtil
import me.clearedSpore.sporeCore.database.util.DocReader
import me.clearedSpore.sporeCore.database.util.DocWriter
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.dizitart.no2.collection.Document
import java.util.Base64
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.bukkit.util.io.BukkitObjectInputStream
import org.bukkit.util.io.BukkitObjectOutputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream


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
        .put("contents", contents.map { it.encodeToBase64() })
        .put("armor", armor.map { it.encodeToBase64() })
        .put("offhand", offhand.encodeToBase64())
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

            val contents = reader.list("contents").mapNotNull { (it as? String)?.decodeItem() }
            val armor = reader.list("armor").mapNotNull { (it as? String)?.decodeItem() }
            val offhand = (doc["offhand"] as? String)?.decodeItem()

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

private fun ItemStack?.encodeToBase64(): String? {
    if (this == null) return null
    return try {
        val output = ByteArrayOutputStream()
        val dataOutput = BukkitObjectOutputStream(output)
        dataOutput.writeObject(this)
        dataOutput.close()
        Base64.getEncoder().encodeToString(output.toByteArray())
    } catch (ex: Exception) {
        ex.printStackTrace()
        null
    }
}

private fun String.decodeItem(): ItemStack? {
    return try {
        val bytes = Base64.getDecoder().decode(this)
        val input = ByteArrayInputStream(bytes)
        val dataInput = BukkitObjectInputStream(input)
        val item = dataInput.readObject() as ItemStack
        dataInput.close()
        item
    } catch (ex: Exception) {
        ex.printStackTrace()
        null
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

