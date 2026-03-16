package com.metoly.morganize.core.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Room database migrations.
 *
 * MIGRATION_1_2 — adds new columns to 'notes' required for the following features:
 * - backgroundColor (colour picker)
 * - categoryId (category filtering)
 * - imagePaths (image attachments, stored as JSON)
 * - drawingPath (hand-drawn sketch file path)
 * - isMarkdownEnabled (markdown / rich-text mode toggle)
 * - checklistJson (checkbox list, stored as JSON) Also creates the new 'categories' table.
 */
val MIGRATION_1_2 =
        object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // New columns on the existing notes table (SQLite ALTER TABLE adds one column at a
                // time)
                db.execSQL("ALTER TABLE notes ADD COLUMN backgroundColor INTEGER")
                db.execSQL("ALTER TABLE notes ADD COLUMN categoryId INTEGER")
                db.execSQL("ALTER TABLE notes ADD COLUMN imagePaths TEXT NOT NULL DEFAULT '[]'")
                db.execSQL("ALTER TABLE notes ADD COLUMN drawingPath TEXT")
                db.execSQL(
                        "ALTER TABLE notes ADD COLUMN isMarkdownEnabled INTEGER NOT NULL DEFAULT 0"
                )
                db.execSQL("ALTER TABLE notes ADD COLUMN checklistJson TEXT NOT NULL DEFAULT ''")

                // New categories table
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
