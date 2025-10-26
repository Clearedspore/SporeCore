package me.clearedSpore.sporeCore.database

import me.clearedSpore.sporeAPI.util.Logger
import me.clearedSpore.sporeCore.user.User
import org.dizitart.no2.Nitrite
import org.dizitart.no2.collection.NitriteCollection
import org.dizitart.no2.mvstore.MVStoreModule
import java.io.File
import java.util.*

object DatabaseManager {
    private lateinit var db: Nitrite
    private lateinit var userCollection: NitriteCollection
    private lateinit var serverCollection: NitriteCollection
    private var serverData: Database? = null

    fun init(pluginFolder: File) {
        if (!pluginFolder.exists()) pluginFolder.mkdirs()
        val dbFile = File(pluginFolder, "sporecore.db")

        db = Nitrite.builder()
            .loadModule(MVStoreModule.withConfig().filePath(dbFile).build())
            .openOrCreate()

        userCollection = db.getCollection("users")
        serverCollection = db.getCollection("server")

        serverData = Database.load(serverCollection)
        Logger.infoDB("Nitrite database ready at ${dbFile.absolutePath}")
    }

    fun getUserCollection(): NitriteCollection = userCollection
    fun getServerCollection(): NitriteCollection = serverCollection

    fun getServerData(): Database = serverData ?: Database()

    fun saveServerData() {
        serverData?.let { db ->
            db.save(serverCollection)
            Logger.infoDB("Saved global server data")
        }
    }

    fun close() {
        saveServerData()
        db.close()
    }
}
