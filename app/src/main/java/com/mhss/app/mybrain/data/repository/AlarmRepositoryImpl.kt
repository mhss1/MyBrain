package com.mhss.app.mybrain.data.repository

import com.mhss.app.mybrain.data.local.dao.AlarmDao
import com.mhss.app.mybrain.domain.model.Alarm
import com.mhss.app.mybrain.domain.repository.AlarmRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class AlarmRepositoryImpl(
    private val alarmDao: AlarmDao,
    private val ioDispatcher: CoroutineDispatcher
) : AlarmRepository {

    override suspend fun getAlarms(): List<Alarm> {
        return withContext(ioDispatcher) {
            alarmDao.getAll()
        }
    }

    override suspend fun insertAlarm(alarm: Alarm) {
        withContext(ioDispatcher) {
            alarmDao.insert(alarm)
        }
    }

    override suspend fun deleteAlarm(alarm: Alarm) {
        withContext(ioDispatcher) {
            alarmDao.delete(alarm)
        }
    }

    override suspend fun deleteAlarm(id: Int) {
        withContext(ioDispatcher) {
            alarmDao.delete(id)
        }
    }
}