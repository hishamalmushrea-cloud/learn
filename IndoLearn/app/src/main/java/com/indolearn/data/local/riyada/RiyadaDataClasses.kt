package com.indolearn.data.local.riyada

import com.google.gson.JsonElement

data class RiyadaSection(
    val id: String,
    val num: String,
    val title: String,
    val color: String
)

data class RiyadaPlanYear(
    val year: Int,
    val title: String,
    val theme: String,
    val quarters: List<RiyadaPlanQuarter>
)

data class RiyadaPlanQuarter(
    val q: String,
    val months: List<RiyadaPlanMonth>
)

data class RiyadaPlanMonth(
    val m: String,
    val weeks: List<RiyadaPlanWeek>
)

data class RiyadaPlanWeek(
    val w: String,
    val tasks: List<String>
)

/**
 * Top-level response. Category fields are kept as raw JsonElement
 * because each section (LEARNING, SALES, etc.) has a unique structure.
 */
data class RiyadaDataResponse(
    val SECTIONS: List<RiyadaSection>,
    val PLAN: List<RiyadaPlanYear>,
    
    // Everything else is raw JsonElement to prevent Gson parse crashes 
    // due to structural mismatches.
    val REFLECTIONS: JsonElement?,
    val LEARNING: JsonElement?,
    val CURRENTJOB: JsonElement?,
    val PERSONALITY: JsonElement?,
    val SALES: JsonElement?,
    val MONEY: JsonElement?,
    val PROJECT: JsonElement?,
    val REPUTATION: JsonElement?,
    val BOOKS: JsonElement?,
    val COURSES: JsonElement?,
    val AI: JsonElement?,
    val HABITS: JsonElement?,
    val MISTAKES: JsonElement?,
    val SCENARIOS: JsonElement?,
    val FUTURE: JsonElement?,
    val DASHBOARD: JsonElement?
)
