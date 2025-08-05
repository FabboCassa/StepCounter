package com.app.stepcounter.domain.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.app.stepcounter.domain.model.Participant // <-- Assicurati di importare la classe Participant

class Converters {
    @TypeConverter
    fun fromParticipantList(value: String?): List<Participant> { // <-- Modificato qui
        if (value == null) {
            return emptyList()
        }
        val listType = object : TypeToken<List<Participant>>() {}.type // <-- E qui
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun toParticipantList(list: List<Participant>?): String { // <-- Modificato qui
        if (list == null) {
            return ""
        }
        return Gson().toJson(list)
    }
}