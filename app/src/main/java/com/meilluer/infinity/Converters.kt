package com.meilluer.infinity

import androidx.room.TypeConverter
import com.meilluer.infinity.comment.DraftType

class Converters {
    @TypeConverter
    fun fromDraftType(value: DraftType): String {
        return value.name
    }

    @TypeConverter
    fun toDraftType(value: String): DraftType {
        return DraftType.valueOf(value)
    }
}