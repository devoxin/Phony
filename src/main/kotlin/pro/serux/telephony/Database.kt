package pro.serux.telephony

import com.zaxxer.hikari.HikariDataSource
import java.sql.Connection

object Database {
    private val pool = HikariDataSource()
    private val connection: Connection
        get() = pool.connection

    init {
        if (!pool.isRunning) {
            pool.jdbcUrl = "jdbc:sqlite:phony.db"
        }

        connection.use {
            val stmt = it.createStatement()
            stmt.addBatch("CREATE TABLE IF NOT EXISTS phonebook (id INTEGER PRIMARY KEY)")
            stmt.addBatch("CREATE TABLE IF NOT EXISTS experiments (id INTEGER PRIMARY KEY, enabled INTEGER)")
            stmt.executeBatch()
        }
    }

    fun phonebookList(): List<Long> {
        connection.use {
            val stmt = it.prepareStatement("SELECT * FROM phonebook")
            val results = stmt.executeQuery()
            val ids = mutableListOf<Long>()

            while (results.next()) {
                ids.add(results.getLong("id"))
            }

            return ids
        }
    }

    fun phonebookOptIn(guildId: Long) {
        if (phonebookHas(guildId)) {
            return
        }

        connection.use {
            val stmt = it.prepareStatement("INSERT INTO phonebook VALUES (?)")
            stmt.setLong(1, guildId)
            stmt.execute()
        }
    }

    fun phonebookOptOut(guildId: Long) {
        if (!phonebookHas(guildId)) {
            return
        }

        connection.use {
            val stmt = it.prepareStatement("DELETE FROM phonebook WHERE id = ?")
            stmt.setLong(1, guildId)
            stmt.execute()
        }
    }

    fun phonebookHas(guildId: Long): Boolean {
        connection.use {
            val stmt = it.prepareStatement("SELECT * FROM phonebook WHERE id = ?")
            stmt.setLong(1, guildId)
            val result = stmt.executeQuery()

            return result.next()
        }
    }

    fun experimentsFor(guildId: Long): Int {
        connection.use {
            val stmt = it.prepareStatement("SELECT * FROM experiments WHERE id = ?")
            stmt.setLong(1, guildId)
            val result = stmt.executeQuery()

            return if (result.next()) result.getInt("enabled") else 0
        }
    }

    fun setExperiments(guildId: Long, experiments: Int) {
        connection.use {
            val stmt = it.prepareStatement("INSERT INTO experiments(id, enabled) VALUES (?, ?) ON CONFLICT(id) DO UPDATE SET enabled = ?")
            stmt.setLong(1, guildId)
            stmt.setInt(2, experiments)
            stmt.setInt(3, experiments)
            stmt.execute()
        }
    }
}
