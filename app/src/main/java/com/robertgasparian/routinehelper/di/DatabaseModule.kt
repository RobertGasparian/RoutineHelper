package com.robertgasparian.routinehelper.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import com.robertgasparian.routinehelper.data.local.RoutineDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideRoutineDatabase(
        @ApplicationContext context: Context,
    ): RoutineDatabase =
        Room.databaseBuilder(
            context,
            RoutineDatabase::class.java,
            "routine-helper.db",
        )
            .addMigrations(MIGRATION_1_2)
            .addMigrations(MIGRATION_2_3)
            .addMigrations(MIGRATION_3_4)
            .build()

    private val MIGRATION_1_2 =
        object : Migration(1, 2) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL("ALTER TABLE routine_items ADD COLUMN cadence TEXT NOT NULL DEFAULT 'DAILY'")
                connection.execSQL("CREATE INDEX IF NOT EXISTS index_routine_items_cadence_position ON routine_items(cadence, position)")
                connection.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS weekly_entries (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        routineItemId INTEGER NOT NULL,
                        weekStartDate TEXT NOT NULL,
                        isChecked INTEGER NOT NULL DEFAULT 0,
                        note TEXT,
                        updatedAtMillis INTEGER NOT NULL,
                        FOREIGN KEY(routineItemId) REFERENCES routine_items(id) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent(),
                )
                connection.execSQL("CREATE INDEX IF NOT EXISTS index_weekly_entries_routineItemId ON weekly_entries(routineItemId)")
                connection.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_weekly_entries_weekStartDate_routineItemId ON weekly_entries(weekStartDate, routineItemId)")
            }
        }

    private val MIGRATION_2_3 =
        object : Migration(2, 3) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL("ALTER TABLE daily_snapshots ADD COLUMN cadence TEXT NOT NULL DEFAULT 'DAILY'")
                connection.execSQL("DROP INDEX IF EXISTS index_daily_snapshots_date")
                connection.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_daily_snapshots_date_cadence ON daily_snapshots(date, cadence)")
            }
        }

    private val MIGRATION_3_4 =
        object : Migration(3, 4) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL("ALTER TABLE actions ADD COLUMN repeatTargetCount INTEGER")
                connection.execSQL("ALTER TABLE today_entries ADD COLUMN completedCount INTEGER NOT NULL DEFAULT 0")
                connection.execSQL("ALTER TABLE weekly_entries ADD COLUMN completedCount INTEGER NOT NULL DEFAULT 0")
                connection.execSQL("ALTER TABLE daily_snapshot_entries ADD COLUMN repeatTargetCountSnapshot INTEGER")
                connection.execSQL("ALTER TABLE daily_snapshot_entries ADD COLUMN completedCount INTEGER NOT NULL DEFAULT 0")
            }
        }
}
