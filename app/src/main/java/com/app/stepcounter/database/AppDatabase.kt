package com.app.stepcounter.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.app.stepcounter.database.dao.PartyDao
import com.app.stepcounter.domain.converter.Converters
import com.app.stepcounter.domain.model.PartyEntity

@Database(entities = [PartyEntity::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun partyDao(): PartyDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "step_counter_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}