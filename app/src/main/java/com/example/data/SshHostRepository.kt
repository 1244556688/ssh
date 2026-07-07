package com.example.data

import kotlinx.coroutines.flow.Flow

class SshHostRepository(private val sshHostDao: SshHostDao) {
    val allHosts: Flow<List<SshHost>> = sshHostDao.getAllHosts()

    suspend fun getHostById(id: Int): SshHost? {
        return sshHostDao.getHostById(id)
    }

    suspend fun insertHost(host: SshHost): Long {
        return sshHostDao.insertHost(host)
    }

    suspend fun updateHost(host: SshHost) {
        sshHostDao.updateHost(host)
    }

    suspend fun deleteHost(host: SshHost) {
        sshHostDao.deleteHost(host)
    }

    suspend fun updateLastConnected(id: Int, timestamp: Long) {
        sshHostDao.updateLastConnected(id, timestamp)
    }
}
