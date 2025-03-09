package com.health.pillreminder.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ScheduleSharedViewModel : ViewModel() {
    private val _periodStart = MutableLiveData<Long>()
    val periodStart: LiveData<Long> get() = _periodStart

    private val _periodEnd = MutableLiveData<Long>()
    val periodEnd: LiveData<Long> get() = _periodEnd

    private val _fixedTime = MutableLiveData<Long>()
    val fixedTime: LiveData<Long> get() = _fixedTime

    private val _singleTimestamp = MutableLiveData<Long>()
    val singleTimestamp: LiveData<Long> get() = _singleTimestamp

    fun setPeriodData(start: Long, end: Long, time: Long) {
        _periodStart.value = start
        _periodEnd.value = end
        _fixedTime.value = time
    }

    fun setSingleTimestamp(timestamp: Long) {
        _singleTimestamp.value = timestamp
    }

    // ✅ Добавляем очистку данных
    fun clearData() {
        _periodStart.value = 0L
        _periodEnd.value = 0L
        _fixedTime.value = 0L
        _singleTimestamp.value = 0L
    }
}
