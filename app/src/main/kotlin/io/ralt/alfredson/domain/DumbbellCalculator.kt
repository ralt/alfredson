package io.ralt.alfredson.domain

import kotlin.math.round

fun calculateDumbbellKg(bodyWeightKg: Double, extraLoadPct: Int): Double {
    if (extraLoadPct <= 0 || bodyWeightKg <= 0.0) return 0.0
    val raw = bodyWeightKg * extraLoadPct / 100.0
    return round(raw * 2.0) / 2.0
}
