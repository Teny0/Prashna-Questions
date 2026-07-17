package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HostDao {
    @Query("SELECT * FROM hosts ORDER BY rating DESC")
    fun getAllHosts(): Flow<List<Host>>

    @Query("SELECT COUNT(*) FROM hosts")
    suspend fun getHostCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHost(host: Host)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHosts(hosts: List<Host>)
}

@Dao
interface JournalEntryDao {
    @Query("SELECT * FROM journal_entries ORDER BY timestamp DESC")
    fun getAllEntries(): Flow<List<JournalEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: JournalEntry)

    @Delete
    suspend fun deleteEntry(entry: JournalEntry)
}

@Dao
interface SessionDao {
    @Query("SELECT * FROM sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<Session>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: Session)
}

@Dao
interface UserBadgeDao {
    @Query("SELECT * FROM user_badges ORDER BY unlockedDate DESC")
    fun getAllBadges(): Flow<List<UserBadge>>

    @Query("SELECT COUNT(*) FROM user_badges WHERE name = :name")
    suspend fun hasBadge(name: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBadge(badge: UserBadge)
}
