package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SshHostDao {
    @Query("SELECT * FROM ssh_hosts ORDER BY isBookmarked DESC, lastConnected DESC, alias ASC")
    fun getAllHosts(): Flow<List<SshHost>>

    @Query("SELECT * FROM ssh_hosts WHERE id = :id")
    suspend fun getHostById(id: Int): SshHost?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHost(host: SshHost): Long

    @Update
    suspend fun updateHost(host: SshHost)

    @Delete
    suspend fun deleteHost(host: SshHost)

    @Query("UPDATE ssh_hosts SET lastConnected = :timestamp WHERE id = :id")
    suspend fun updateLastConnected(id: Int, timestamp: Long)
}
