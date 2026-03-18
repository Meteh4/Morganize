package com.metoly.morganize.core.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Room database migrations.
 *
 * MIGRATION_1_2 — adds new columns to 'notes' required for the following features:
 * - backgroundColor, categoryId, imagePaths, drawingPath, isMarkdownEnabled, checklistJson
 * - Also creates the 'categories' table.
 *
 * MIGRATION_2_3 — replaces the Markdown system with native Rich Text:
 * - richSpansJson: JSON-serialized List<RichSpan> describing formatting ranges
 * - isMarkdownEnabled column is kept for backward compatibility but no longer used
 */
val MIGRATION_1_2 =
        object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE notes ADD COLUMN backgroundColor INTEGER")
                db.execSQL("ALTER TABLE notes ADD COLUMN categoryId INTEGER")
                db.execSQL("ALTER TABLE notes ADD COLUMN imagePaths TEXT NOT NULL DEFAULT '[]'")
                db.execSQL("ALTER TABLE notes ADD COLUMN drawingPath TEXT")
                db.execSQL(
                        "ALTER TABLE notes ADD COLUMN isMarkdownEnabled INTEGER NOT NULL DEFAULT 0"
                )
                db.execSQL("ALTER TABLE notes ADD COLUMN checklistJson TEXT NOT NULL DEFAULT ''")

                db.execSQL(
                        """
            CREATE TABLE IF NOT EXISTS `categories` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `name` TEXT NOT NULL,
                `colorArgb` INTEGER NOT NULL
            )
            """.trimIndent()
                )
            }
        }

val MIGRATION_2_3 =
        object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add rich text span storage column (plain JSON, default empty)
                db.execSQL("ALTER TABLE notes ADD COLUMN richSpansJson TEXT NOT NULL DEFAULT ''")
            }
        }
