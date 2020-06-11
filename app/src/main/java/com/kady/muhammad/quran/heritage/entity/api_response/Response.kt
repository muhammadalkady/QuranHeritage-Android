package com.kady.muhammad.quran.heritage.entity.api_response

import org.json.JSONException
import org.json.JSONObject

interface Response

fun String.isSuccess(): Boolean {
    val json = JSONObject(this)
    return try {
        json.get("data");true
    } catch (e: JSONException) {
        false
    }
}