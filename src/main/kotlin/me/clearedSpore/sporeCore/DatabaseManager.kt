package me.clearedSpore.sporeCore

import me.clearedSpore.sporeAPI.util.Logger
import me.clearedSpore.sporeCore.features.logs.LogsService
import me.clearedSpore.sporeCore.inventory.InventoryManager
import org.dizitart.no2.Nitrite
import org.dizitart.no2.collection.NitriteCollection
import org.dizitart.no2.mvstore.MVStoreModule
import java.io.File

object DatabaseManager {
    private lateinit var db: Nitrite

    private lateinit var userCollection: NitriteCollection
    private lateinit var serverCollection: NitriteCollection
    private lateinit var inventoryCollection: NitriteCollection
    private lateinit var logsCollection: NitriteCollection
    private lateinit var reportCollection: NitriteCollection
    private lateinit var investigationCollection: NitriteCollection

    private lateinit var serverData: Database

    fun init(pluginFolder: File) {
        if (!pluginFolder.exists()) pluginFolder.mkdirs()
        val dbFile = File(pluginFolder, "sporecore.db")

        db = Nitrite.builder()
            .loadModule(MVStoreModule.withConfig().filePath(dbFile).build())
            .openOrCreate()


        Logger.infoDB("Loading users")
        try {
            userCollection = db.getCollection("users")
            Logger.infoDB("Users loaded")
        } catch (e: Exception) {
            e.printStackTrace()
        }

        Logger.infoDB("Loading server data")
        try {
            serverCollection = db.getCollection("server")
            Logger.infoDB("Server data loaded")
        } catch (e: Exception) {
            e.printStackTrace()
        }

        Logger.infoDB("Loading inventories")
        try {
            inventoryCollection = db.getCollection("inventories")
            Logger.infoDB("Inventories loaded!")
        } catch (e: Exception) {
            e.printStackTrace()
        }

        Logger.infoDB("Loading Logs")
        try {
            logsCollection = db.getCollection("Logs")
            LogsService.initializeLogs()
            Logger.infoDB("Logs loaded!")
        } catch (e: Exception) {
            e.printStackTrace()
        }

        Logger.infoDB("Loading Reports")
        try {
            reportCollection = db.getCollection("reports")
            Logger.infoDB("Reports loaded!")
        } catch (e: Exception) {
            e.printStackTrace()
        }


        Logger.infoDB("Loading Investigations")
        try {
            investigationCollection = db.getCollection("investigations")
            Logger.infoDB("Investigations loaded!")
        } catch (e: Exception) {
            e.printStackTrace()
        }

        serverData = Database.load(serverCollection)
        InventoryManager.loadAllInventories()
        Logger.infoDB("Nitrite database ready at ${dbFile.absolutePath}")
    }

    fun getUserCollection(): NitriteCollection = userCollection
    fun getServerCollection(): NitriteCollection = serverCollection
    fun getInventoryCollection(): NitriteCollection = inventoryCollection
    fun getLogsCollection(): NitriteCollection = logsCollection
    fun getReportCollection(): NitriteCollection = reportCollection
    fun getInvestigationCollection(): NitriteCollection = investigationCollection

    fun getServerData(): Database = serverData

    fun saveServerData() {
        serverData.save(serverCollection)
        Logger.infoDB("Saved global server data")
    }

    fun close() {
        InventoryManager.saveAllInventories()
        saveServerData()
        db.close()
    }
}
