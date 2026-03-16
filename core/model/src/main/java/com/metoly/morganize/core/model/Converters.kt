package com.metoly.morganize.core.model

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Room [TypeConverter]s for complex types stored in the database.
 *
 * - [List<String>] (image paths) ↔ JSON String
 */
class Converters {

    @TypeConverter fun fromStringList(list: List<String>): String = Json.encodeToString(list)

    @TypeConverter
    fun toStringList(json: String): List<String> =
            if (json.isBlank()) emptyList() else Json.decodeFromString(json)
}
