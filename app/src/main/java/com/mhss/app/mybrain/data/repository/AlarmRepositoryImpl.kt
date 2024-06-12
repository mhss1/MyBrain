package com.mhss.app.mybrain.data.repository

import com.mhss.app.mybrain.data.local.dao.AlarmDao
import com.mhss.app.mybrain.data.local.entity.toAlarm
import com.mhss.app.mybrain.data.local.entity.toAlarmEntity
import com.mhss.app.mybrain.di.namedIoDispatcher
import com.mhss.app.mybrain.domain.model.alarm.Alarm
import com.mhss.app.mybrain.domain.repository.alarms.AlarmRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Single
class AlarmRepositoryImpl(
    private val alarmDao: AlarmDao,
    @Named(namedIoDispatcher)private val ioDispatcher: CoroutineDispatcher
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