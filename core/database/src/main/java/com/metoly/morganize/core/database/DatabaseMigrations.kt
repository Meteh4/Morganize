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

/**
 * MIGRATION_3_4 — Grid system migration:
 * Consolidates content, imagePaths, drawingPath, richSpansJson, checklistJson
 * into a single pagesJson column. Old note content cannot be losslessly converted
 * to the new grid format, so notes are preserved with metadata but empty pages.
 */
val MIGRATION_3_4 =
        object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS notes_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        title TEXT NOT NULL,
                        pagesJson TEXT NOT NULL DEFAULT '[]',
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL,
                        backgroundColor INTEGER,
                        categoryId INTEGER
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO notes_new (id, title, createdAt, updatedAt, backgroundColor, categoryId)
                    SELECT id, title, createdAt, updatedAt, backgroundColor, categoryId FROM notes
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE notes")
                db.execSQL("ALTER TABLE notes_new RENAME TO notes")
            }
        }

/**
 * MIGRATION_4_5 — adds SecretNote encryption support:
 * - isSecret: flag indicating the note is password/biometric-protected
 * - encryptedContent: Base64-encoded AES-256-GCM ciphertext of pages JSON
 * - salt: Base64-encoded PBKDF2 salt
 * - iv: Base64-encoded GCM nonce (initialization vector)
 */
val MIGRATION_4_5 =
        object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE notes ADD COLUMN isSecret INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE notes ADD COLUMN encryptedContent TEXT")
                db.execSQL("ALTER TABLE notes ADD COLUMN salt TEXT")
                db.execSQL("ALTER TABLE notes ADD COLUMN iv TEXT")
            }
        }

/**
 * MIGRATION_5_6 — adds Biometric support to SecretNote:
 * - hasBiometric: flag indicating biometric unlock is enabled
 * - biometricWrappedPassword: AES-256-GCM ciphertext of the master password using hardware keystore key
 * - biometricWrappedPasswordIv: GCM nonce for the wrapped password
 */
val MIGRATION_5_6 =
        object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE notes ADD COLUMN hasBiometric INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE notes ADD COLUMN biometricWrappedPassword TEXT")
                db.execSQL("ALTER TABLE notes ADD COLUMN biometricWrappedPasswordIv TEXT")
            }
        }

/**
 * MIGRATION_6_7 — Faz 1 & 2 feature support:
 * - isPinned: allows users to pin important notes to the top (F-2)
 * - isDeleted / deletedAt: soft-delete and trash bin support (F-3)
 * - isArchived: note archiving for decluttering without deletion (F-8)
 */
val MIGRATION_6_7 =
        object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE notes ADD COLUMN isPinned INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE notes ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE notes ADD COLUMN deletedAt INTEGER")
                db.execSQL("ALTER TABLE notes ADD COLUMN isArchived INTEGER NOT NULL DEFAULT 0")
            }
        }

/**
 * MIGRATION_7_8 — Faz 3 feature support:
 * - tags table: lightweight labels for notes (F-15)
 * - note_tag_cross_ref: many-to-many note↔tag relationship (F-15)
 * - reminderAt: optional reminder timestamp on notes (F-16)
 */
val MIGRATION_7_8 =
        object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `tags` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `colorArgb` INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `note_tag_cross_ref` (
                        `noteId` INTEGER NOT NULL,
                        `tagId` INTEGER NOT NULL,
                        PRIMARY KEY(`noteId`, `tagId`),
                        FOREIGN KEY(`noteId`) REFERENCES `notes`(`id`) ON DELETE CASCADE,
                        FOREIGN KEY(`tagId`) REFERENCES `tags`(`id`) ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("ALTER TABLE notes ADD COLUMN reminderAt INTEGER")
            }
        }
