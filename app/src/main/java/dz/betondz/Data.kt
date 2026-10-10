package dz.betondz

import androidx.room.*

@Entity(tableName = "entries")
data class SiteEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String, val details: String, val totalDzd: Double,
    val createdAt: Long = System.currentTimeMillis()
)

@Dao
interface EntryDao {
    @Query("SELECT * FROM entries ORDER BY createdAt DESC") suspend fun all(): List<SiteEntry>
    @Insert suspend fun insert(e: SiteEntry)
    @Query("DELETE FROM entries WHERE id = :id") suspend fun delete(id: Long)
}

@Database(entities = [SiteEntry::class], version = 1, exportSchema = false)
abstract class AppDb : RoomDatabase() { abstract fun entries(): EntryDao }
