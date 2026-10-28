package com.calculadoracarrera.app.domain

import java.util.Locale
import kotlin.math.roundToLong

/** Los tres campos de la calculadora. */
enum class Field { DISTANCE, TIME, PACE }

/** Distancias predefinidas, en km. */
enum class Preset(val km: Double) {
    FIVE_K(5.0),
    TEN_K(10.0),
    HALF_MARATHON(21.0975),
    MARATHON(42.195),
}

enum class CalcError {
    NOT_ENOUGH_FIELDS,
    ALL_FIELDS_FILLED,
    PACE_OUT_OF_RANGE,
    TIME_OUT_OF_RANGE,
    DISTANCE_OUT_OF_RANGE,
}

sealed interface CalcResult {
    /**
     * Resultado completo. [exactPaceSecondsPerKm] es el ritmo sin redondear,
     * útil para la velocidad y los tiempos estimados.
     */
    data class Success(
        val calculated: Field,
        val distanceKm: Double,
        val timeSeconds: Long,
        val paceSeconds: Long,
        val exactPaceSecondsPerKm: Double,
    ) : CalcResult

    data class Failure(val error: CalcError) : CalcResult
}

data class Hms(val hours: Long, val minutes: Long, val seconds: Long)
data class MinSec(val minutes: Long, val seconds: Long)
data class KmM(val km: Long, val meters: Long)

/** Lógica pura de la calculadora, sin dependencias de Android. */
object RaceCalculator {
    const val MIN_PACE_SECONDS = 90L          // 1:30 /km
    const val MAX_PACE_SECONDS = 30L * 60     // 30:00 /km
    const val MAX_TIME_SECONDS = 99L * 3600 + 59 * 60 + 59
    const val MAX_DISTANCE_KM = 999.999

    fun paceSeconds(distanceKm: Double, timeSeconds: Long): Long =
        (timeSeconds / distanceKm).roundToLong()

    fun timeSeconds(distanceKm: Double, paceSeconds: Double): Long =
        (paceSeconds * distanceKm).roundToLong()

    /** Distancia en km, redondeada al metro. */
    fun distanceKm(timeSeconds: Long, paceSeconds: Long): Double =
        roundToMeters(timeSeconds.toDouble() / paceSeconds)

    fun roundToMeters(km: Double): Double = (km * 1000).roundToLong() / 1000.0

    fun speedKmh(exactPaceSecondsPerKm: Double): Double = 3600.0 / exactPaceSecondsPerKm

    /**
     * Calcula el campo que falta. Un valor <= 0 significa "campo vacío".
     * Si los tres están rellenos, recalcula [lastCalculated].
     */
    fun calculate(
        distanceKm: Double,
        timeSeconds: Long,
        paceSeconds: Long,
        lastCalculated: Field? = null,
    ): CalcResult {
        val filled = buildSet {
            if (distanceKm > 0) add(Field.DISTANCE)
            if (timeSeconds > 0) add(Field.TIME)
            if (paceSeconds > 0) add(Field.PACE)
        }
        val target = when (filled.size) {
            3 -> lastCalculated ?: return CalcResult.Failure(CalcError.ALL_FIELDS_FILLED)
            2 -> Field.entries.first { it !in filled }
            else -> return CalcResult.Failure(CalcError.NOT_ENOUGH_FIELDS)
        }

        val result = when (target) {
            Field.DISTANCE -> CalcResult.Success(
                calculated = Field.DISTANCE,
                distanceKm = distanceKm(timeSeconds, paceSeconds),
                timeSeconds = timeSeconds,
                paceSeconds = paceSeconds,
                exactPaceSecondsPerKm = paceSeconds.toDouble(),
            )
            Field.TIME -> CalcResult.Success(
                calculated = Field.TIME,
                distanceKm = distanceKm,
                timeSeconds = timeSeconds(distanceKm, paceSeconds.toDouble()),
                paceSeconds = paceSeconds,
                exactPaceSecondsPerKm = paceSeconds.toDouble(),
            )
            Field.PACE -> CalcResult.Success(
                calculated = Field.PACE,
                distanceKm = distanceKm,
                timeSeconds = timeSeconds,
                paceSeconds = paceSeconds(distanceKm, timeSeconds),
                exactPaceSecondsPerKm = timeSeconds / distanceKm,
            )
        }
        return validate(result)
    }

    private fun validate(r: CalcResult.Success): CalcResult = when {
        r.paceSeconds !in MIN_PACE_SECONDS..MAX_PACE_SECONDS ->
            CalcResult.Failure(CalcError.PACE_OUT_OF_RANGE)
        r.timeSeconds !in 1..MAX_TIME_SECONDS ->
            CalcResult.Failure(CalcError.TIME_OUT_OF_RANGE)
        r.distanceKm <= 0.0 || r.distanceKm > MAX_DISTANCE_KM ->
            CalcResult.Failure(CalcError.DISTANCE_OUT_OF_RANGE)
        else -> r
    }

    /** Tiempo estimado (s) para cada distancia predefinida al ritmo dado. */
    fun estimates(exactPaceSecondsPerKm: Double): Map<Preset, Long> =
        Preset.entries.associateWith { timeSeconds(it.km, exactPaceSecondsPerKm) }

    // ---- Conversión y normalización ----

    fun toHms(totalSeconds: Long): Hms =
        Hms(totalSeconds / 3600, (totalSeconds % 3600) / 60, totalSeconds % 60)

    fun toMinSec(totalSeconds: Long): MinSec = MinSec(totalSeconds / 60, totalSeconds % 60)

    fun toKmM(km: Double): KmM {
        val meters = (km * 1000).roundToLong()
        return KmM(meters / 1000, meters % 1000)
    }

    fun hmsToSeconds(h: Long, m: Long, s: Long): Long = h * 3600 + m * 60 + s

    // ---- Formato para mostrar ----

    fun formatDuration(totalSeconds: Long): String {
        val (h, m, s) = toHms(totalSeconds)
        return if (h > 0) String.format(Locale.ROOT, "%d:%02d:%02d", h, m, s)
        else String.format(Locale.ROOT, "%d:%02d", m, s)
    }

    fun formatPace(paceSeconds: Long): String {
        val (m, s) = toMinSec(paceSeconds)
        return String.format(Locale.ROOT, "%d:%02d /km", m, s)
    }

    fun formatSpeed(kmh: Double): String =
        String.format(Locale.forLanguageTag("es-ES"), "%.2f km/h", kmh)
}
