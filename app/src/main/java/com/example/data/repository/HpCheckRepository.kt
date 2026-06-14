package com.example.data.repository

import com.example.data.dao.HpCheckDao
import com.example.data.entity.HpCheckReport
import kotlinx.coroutines.flow.Flow

class HpCheckRepository(private val hpCheckDao: HpCheckDao) {
    val allReports: Flow<List<HpCheckReport>> = hpCheckDao.getAllReports()

    fun getReportById(id: Int): Flow<HpCheckReport?> {
        return hpCheckDao.getReportById(id)
    }

    suspend fun insertReport(report: HpCheckReport): Long {
        return hpCheckDao.insertReport(report)
    }

    suspend fun updateReport(report: HpCheckReport) {
        hpCheckDao.updateReport(report)
    }

    suspend fun deleteReport(report: HpCheckReport) {
        hpCheckDao.deleteReport(report)
    }

    suspend fun deleteReportById(id: Int) {
        hpCheckDao.deleteReportById(id)
    }
}
