package com.example.data.model

import org.json.JSONArray
import org.json.JSONObject

object SubTaskHelper {
    fun toJson(subTasks: List<SubTask>): String {
        val array = JSONArray()
        for (item in subTasks) {
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("text", item.text)
            obj.put("isCompleted", item.isCompleted)
            array.put(obj)
        }
        return array.toString()
    }

    fun fromJson(json: String?): List<SubTask> {
        if (json.isNullOrBlank()) return emptyList()
        val list = mutableListOf<SubTask>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    SubTask(
                        id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                        text = obj.optString("text", ""),
                        isCompleted = obj.optBoolean("isCompleted", false)
                    )
                )
            }
        } catch (_: Exception) {
            // fallback
        }
        return list
    }
}
