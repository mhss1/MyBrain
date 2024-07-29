package com.mhss.app.mybrain.data.repository

import com.mhss.app.alarm.model.Alarm
import com.mhss.app.alarm.repository.AlarmRepository
import com.mhss.app.database.dao.AlarmDao
import com.mhss.app.database.entity.toAlarm
import com.mhss.app.database.entity.toAlarmEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Single
class AlarmRepositoryImpl(
    private val alarmDao: AlarmDao,
    @Named("ioDispatcher")private val ioDispatcher: CoroutineDispatcher
) : AlarmRepository {

    override suspend fun getAlarms(): List<Alarm> {
        return withContext(ioDispatcher) {
            alarmDao.getAll().map { it.toAlarm() }
        }
    }

    override suspend fun insertAlarm(alarm: Alarm) {
        withContext(ioDispatcher) {
            alarmDao.insert(alarm.toAlarmEntity())
        }
    }

    override suspend fun deleteAlarm(alarm: Alarm) {
        withContext(ioDispatcher) {
            alarmDao.delete(alarm.toAlarmEntity())
        }
    }

    override suspend fun deleteAlarm(id: Int) {
        withContext(ioDispatcher) {
            alarmDao.delete(id)
        }
    }
}