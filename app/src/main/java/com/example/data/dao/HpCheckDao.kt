package com.example.data.dao

import androidx.room.*
import com.example.data.entity.HpCheckReport
import kotlinx.coroutines.flow.Flow

@Dao
interface HpCheckDao {
    @Query("SELECT * FROM hp_check_reports ORDER BY timestamp DESC")
    fun getAllReports(): Flow<List<HpCheckReport>>

    @Query("SELECT * FROM hp_check_reports WHERE id = :id LIMIT 1")
    fun getReportById(id: Int): Flow<HpCheckReport?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: HpCheckReport): Long

    @Update
    suspend fun updateReport(report: HpCheckReport)

    @Delete
    suspend fun deleteReport(report: HpCheckReport)

    @Query("DELETE FROM hp_check_reports WHERE id = :id")
    suspend fun deleteReportById(id: Int)
}
