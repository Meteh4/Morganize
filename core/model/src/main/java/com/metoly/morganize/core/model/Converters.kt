package com.metoly.morganize.core.model

import androidx.room.TypeConverter
import kotlinx.serialization.json.Json

/**
 * Room [TypeConverter]s for complex types stored in the database.
 *
 * Currently converts [List]<[String]> (e.g. legacy image path lists) to/from JSON.
 * Rich-text spans and grid pages are stored as raw JSON strings directly on their
 * respective model fields, so they do not need a Room TypeConverter.
 */
class Converters {

    @TypeConverter fun fromStringList(list: List<String>): String = Json.encodeToString(list)

    @TypeConverter
    fun toStringList(json: String): List<String> =
            if (json.isBlank()) emptyList() else Json.decodeFromString(json)

}
