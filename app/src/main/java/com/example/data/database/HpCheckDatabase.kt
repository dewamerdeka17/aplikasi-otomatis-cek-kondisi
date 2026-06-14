package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.entity.HpCheckReport
import com.example.data.dao.HpCheckDao

@Database(entities = [HpCheckReport::class], version = 3, exportSchema = false)
abstract class HpCheckDatabase : RoomDatabase() {
    abstract fun hpCheckDao(): HpCheckDao

    companion object {
        @Volatile
        private var INSTANCE: HpCheckDatabase? = null

        fun getDatabase(context: Context): HpCheckDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HpCheckDatabase::class.java,
                    "hp_check_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
