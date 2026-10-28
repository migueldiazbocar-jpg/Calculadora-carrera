package com.calculadoracarrera.app.ui

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import com.calculadoracarrera.app.R
import com.calculadoracarrera.app.domain.InputBox
import com.calculadoracarrera.app.domain.CalcError
import com.calculadoracarrera.app.domain.CalcResult
import com.calculadoracarrera.app.domain.Field
import com.calculadoracarrera.app.domain.InputRules
import com.calculadoracarrera.app.domain.Preset
import com.calculadoracarrera.app.domain.RaceCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class UiState(
    val values: Map<InputBox, String> = InputBox.entries.associateWith { "" },
    val preset: Preset? = null,
    val lastCalculated: Field? = null,
    /** Campo recién calculado, que se resalta un momento. */
    val highlighted: Field? = null,
    val result: CalcResult.Success? = null,
    @StringRes val message: Int? = null,
) {
    fun value(box: InputBox): String = values[box].orEmpty()
}

class RaceCalculatorViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    /**
     * Actualiza una casilla. Devuelve true si la casilla se ha completado
     * (para que la interfaz pase el foco a la siguiente).
     */
    fun onValueChange(box: InputBox, raw: String): Boolean {
        val old = _uiState.value.value(box)
        val new = InputRules.sanitize(old, raw, box)
        if (new == old) return false
        _uiState.update { s ->
            s.copy(
                values = s.values + (box to new),
                preset = if (box.field == Field.DISTANCE) null else s.preset,
                highlighted = null,
                result = null,
            )
        }
        return new.length == box.maxDigits && raw.length > old.length
    }

    fun selectPreset(preset: Preset) {
        val kmM = RaceCalculator.toKmM(preset.km)
        _uiState.update { s ->
            s.copy(
                values = s.values +
                    (InputBox.DIST_KM to InputRules.pad(kmM.km, InputBox.DIST_KM)) +
                    (InputBox.DIST_M to InputRules.pad(kmM.meters, InputBox.DIST_M)),
                preset = preset,
                highlighted = null,
                result = null,
            )
        }
    }

    fun calculate() {
        val s = _uiState.value
        val p = { box: InputBox -> InputRules.parse(s.value(box)) }
        val distanceKm = s.preset?.km ?: (p(InputBox.DIST_KM) + p(InputBox.DIST_M) / 1000.0)
        val timeSeconds = RaceCalculator.hmsToSeconds(p(InputBox.HOURS), p(InputBox.MINUTES), p(InputBox.SECONDS))
        val paceSeconds = p(InputBox.PACE_MIN) * 60 + p(InputBox.PACE_SEC)

        when (val r = RaceCalculator.calculate(distanceKm, timeSeconds, paceSeconds, s.lastCalculated)) {
            is CalcResult.Failure -> _uiState.update { it.copy(message = r.error.messageRes()) }
            is CalcResult.Success -> _uiState.update { it.withResult(r) }
        }
    }

    private fun UiState.withResult(r: CalcResult.Success): UiState {
        val newValues = when (r.calculated) {
            Field.DISTANCE -> {
                val d = RaceCalculator.toKmM(r.distanceKm)
                mapOf(
                    InputBox.DIST_KM to InputRules.pad(d.km, InputBox.DIST_KM),
                    InputBox.DIST_M to InputRules.pad(d.meters, InputBox.DIST_M),
                )
            }
            Field.TIME -> {
                val t = RaceCalculator.toHms(r.timeSeconds)
                mapOf(
                    InputBox.HOURS to InputRules.pad(t.hours, InputBox.HOURS),
                    InputBox.MINUTES to InputRules.pad(t.minutes, InputBox.MINUTES),
                    InputBox.SECONDS to InputRules.pad(t.seconds, InputBox.SECONDS),
                )
            }
            Field.PACE -> {
                val pc = RaceCalculator.toMinSec(r.paceSeconds)
                mapOf(
                    InputBox.PACE_MIN to InputRules.pad(pc.minutes, InputBox.PACE_MIN),
                    InputBox.PACE_SEC to InputRules.pad(pc.seconds, InputBox.PACE_SEC),
                )
            }
        }
        return copy(
            values = values + newValues,
            preset = if (r.calculated == Field.DISTANCE) null else preset,
            lastCalculated = r.calculated,
            highlighted = r.calculated,
            result = r,
        )
    }

    fun clear() = _uiState.update { UiState() }

    fun messageShown() = _uiState.update { it.copy(message = null) }

    fun highlightShown() = _uiState.update { it.copy(highlighted = null) }

    private fun CalcError.messageRes(): Int = when (this) {
        CalcError.NOT_ENOUGH_FIELDS -> R.string.error_not_enough_fields
        CalcError.ALL_FIELDS_FILLED -> R.string.error_all_fields_filled
        CalcError.PACE_OUT_OF_RANGE -> R.string.error_pace_out_of_range
        CalcError.TIME_OUT_OF_RANGE -> R.string.error_time_out_of_range
        CalcError.DISTANCE_OUT_OF_RANGE -> R.string.error_distance_out_of_range
    }
}
