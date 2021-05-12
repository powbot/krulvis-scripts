package org.powbot.krulvis.api.script

import com.google.gson.Gson
import java.io.Serializable

interface ScriptProfile : Serializable {

    fun toJson(gson: Gson): String = gson.toJson(this)

}