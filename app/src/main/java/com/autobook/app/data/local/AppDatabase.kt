package com.autobook.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.autobook.app.data.local.dao.FuelLogDao
import com.autobook.app.data.local.dao.ServiceRecordDao
import com.autobook.app.data.local.dao.ServiceReminderDao
import com.autobook.app.data.local.dao.VehicleDao
import com.autobook.app.data.local.dao.WorkshopDao
import com.autobook.app.data.local.entity.FuelLog
import com.autobook.app.data.local.entity.ServiceRecord
import com.autobook.app.data.local.entity.ServiceReminder
import com.autobook.app.data.local.entity.Vehicle
import com.autobook.app.data.local.entity.Workshop

@Database(
    entities = [
        Vehicle::class,
        ServiceRecord::class,
        ServiceReminder::class,
        FuelLog::class,
        Workshop::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun vehicleDao(): VehicleDao
    abstract fun serviceRecordDao(): ServiceRecordDao
    abstract fun serviceReminderDao(): ServiceReminderDao
    abstract fun fuelLogDao(): FuelLogDao
    abstract fun workshopDao(): WorkshopDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * v1 -> v2: adds ServiceReminder.serviceType (which service type a reminder is for,
         * to support multiple auto-generated reminders per service). Existing rows default
         * to empty string. Non-destructive — preserves all user data.
         */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE service_reminder ADD COLUMN serviceType TEXT NOT NULL DEFAULT ''"
                )
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "autobook.db"
                )
                    // Room enables the foreign_keys pragma automatically so that the
                    // ForeignKey CASCADE rules defined on the entities are enforced.
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
