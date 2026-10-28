@file:OptIn(ExperimentalLayoutApi::class)

package com.calculadoracarrera.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.calculadoracarrera.app.R
import com.calculadoracarrera.app.domain.InputBox
import com.calculadoracarrera.app.domain.CalcResult
import com.calculadoracarrera.app.domain.Field
import com.calculadoracarrera.app.domain.Preset
import com.calculadoracarrera.app.domain.RaceCalculator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val BoxSize = 76.dp
private const val WIDE_LAYOUT_MIN_DP = 720

@Composable
fun RaceCalculatorScreen(vm: RaceCalculatorViewModel = viewModel()) {
    val state by vm.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val focusRequesters = remember { InputBox.entries.associateWith { FocusRequester() } }

    // Avisos: se lanzan en un scope propio para que no se cancelen al limpiar el mensaje.
    LaunchedEffect(state.message) {
        state.message?.let { res ->
            vm.messageShown()
            scope.launch {
                snackbarHostState.currentSnackbarData?.dismiss()
                snackbarHostState.showSnackbar(context.getString(res))
            }
        }
    }
    // El resaltado del campo calculado dura un momento.
    LaunchedEffect(state.highlighted) {
        if (state.highlighted != null) {
            delay(1500)
            vm.highlightShown()
        }
    }

    val onBoxChange: (InputBox, String) -> Unit = { box, raw ->
        if (vm.onValueChange(box, raw)) {
            val next = InputBox.entries.getOrNull(box.ordinal + 1)
            if (next != null) focusRequesters.getValue(next).requestFocus()
            else focusManager.clearFocus()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 640.dp),
            )
            Spacer(Modifier.height(28.dp))

            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val wide = maxWidth >= WIDE_LAYOUT_MIN_DP.dp
                val distance = @Composable {
                    DistanceBlock(state, focusRequesters, onBoxChange, vm::selectPreset)
                }
                val time = @Composable { TimeBlock(state, focusRequesters, onBoxChange) }
                val pace = @Composable { PaceBlock(state, focusRequesters, onBoxChange) }

                if (wide) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Top,
                    ) {
                        distance(); VerticalRedLine(); time(); VerticalRedLine(); pace()
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        distance(); HorizontalRedLine(); time(); HorizontalRedLine(); pace()
                    }
                }
            }

            Spacer(Modifier.height(28.dp))
            Button(
                onClick = { focusManager.clearFocus(); vm.calculate() },
                modifier = Modifier.widthIn(min = 240.dp).height(56.dp),
            ) {
                Text(stringResource(R.string.calculate), fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = { focusManager.clearFocus(); vm.clear() },
                modifier = Modifier.widthIn(min = 240.dp),
            ) {
                Text(stringResource(R.string.clear))
            }

            state.result?.let { result ->
                Spacer(Modifier.height(24.dp))
                ExtrasCard(result)
            }
        }
    }
}

// ---------- Bloques ----------

@Composable
private fun DistanceBlock(
    state: UiState,
    focus: Map<InputBox, FocusRequester>,
    onChange: (InputBox, String) -> Unit,
    onPreset: (Preset) -> Unit,
) {
    val hl = state.highlighted == Field.DISTANCE
    FieldBlock(title = stringResource(R.string.distance)) {
        Row(verticalAlignment = Alignment.Top) {
            DigitBox(InputBox.DIST_KM, state, hl, stringResource(R.string.label_km), focus, onChange)
            Colon()
            DigitBox(InputBox.DIST_M, state, hl, stringResource(R.string.label_m), focus, onChange)
        }
        Spacer(Modifier.height(8.dp))
        FlowRow(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.widthIn(max = 340.dp),
        ) {
            Preset.entries.forEach { preset ->
                PresetOption(
                    label = stringResource(preset.labelRes()),
                    selected = state.preset == preset,
                    onClick = { onPreset(preset) },
                )
            }
        }
    }
}

@Composable
private fun TimeBlock(state: UiState, focus: Map<InputBox, FocusRequester>, onChange: (InputBox, String) -> Unit) {
    val hl = state.highlighted == Field.TIME
    FieldBlock(title = stringResource(R.string.time)) {
        Row(verticalAlignment = Alignment.Top) {
            DigitBox(InputBox.HOURS, state, hl, stringResource(R.string.label_hours), focus, onChange)
            Colon()
            DigitBox(InputBox.MINUTES, state, hl, stringResource(R.string.label_min), focus, onChange)
            Colon()
            DigitBox(InputBox.SECONDS, state, hl, stringResource(R.string.label_sec), focus, onChange)
        }
    }
}

@Composable
private fun PaceBlock(state: UiState, focus: Map<InputBox, FocusRequester>, onChange: (InputBox, String) -> Unit) {
    val hl = state.highlighted == Field.PACE
    FieldBlock(title = stringResource(R.string.pace)) {
        Row(verticalAlignment = Alignment.Top) {
            DigitBox(InputBox.PACE_MIN, state, hl, stringResource(R.string.label_min), focus, onChange)
            Colon()
            DigitBox(InputBox.PACE_SEC, state, hl, stringResource(R.string.label_sec), focus, onChange)
            Box(Modifier.height(BoxSize).padding(start = 6.dp), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.per_km), style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}

@Composable
private fun FieldBlock(title: String, content: @Composable () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 8.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(12.dp))
        content()
    }
}

// ---------- Piezas ----------

@Composable
private fun DigitBox(
    box: InputBox,
    state: UiState,
    highlighted: Boolean,
    label: String,
    focus: Map<InputBox, FocusRequester>,
    onChange: (InputBox, String) -> Unit,
) {
    val value = state.value(box)
    val colors = MaterialTheme.colorScheme
    val background by animateColorAsState(
        targetValue = if (highlighted) colors.primary.copy(alpha = 0.35f) else colors.surfaceVariant,
        label = "boxBackground",
    )
    var selection by remember { mutableStateOf(TextRange.Zero) }
    var focused by remember { mutableStateOf(false) }
    val safeSelection = TextRange(
        selection.start.coerceIn(0, value.length),
        selection.end.coerceIn(0, value.length),
    )
    val isLast = box == InputBox.entries.last()
    val focusManager = LocalFocusManager.current

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(BoxSize)
                .background(background, RoundedCornerShape(6.dp))
                .border(
                    width = if (focused) 2.dp else 1.dp,
                    color = if (focused) colors.primary else colors.outline,
                    shape = RoundedCornerShape(6.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            BasicTextField(
                value = TextFieldValue(value, safeSelection),
                onValueChange = { new ->
                    selection = new.selection
                    onChange(box, new.text)
                },
                singleLine = true,
                textStyle = digitStyle(colors.onSurface),
                cursorBrush = SolidColor(colors.primary),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.NumberPassword,
                    imeAction = if (isLast) ImeAction.Done else ImeAction.Next,
                ),
                keyboardActions = KeyboardActions(
                    onNext = { InputBox.entries.getOrNull(box.ordinal + 1)?.let { focus.getValue(it).requestFocus() } },
                    onDone = { focusManager.clearFocus() },
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focus.getValue(box))
                    .onFocusChanged {
                        focused = it.isFocused
                        // Al entrar en la casilla se selecciona todo para sobrescribir.
                        if (it.isFocused) selection = TextRange(0, value.length)
                    },
                decorationBox = { inner ->
                    Box(contentAlignment = Alignment.Center) {
                        if (value.isEmpty()) {
                            Text(
                                text = "0".repeat(if (box == InputBox.DIST_KM || box == InputBox.DIST_M) 3 else 2),
                                style = digitStyle(colors.onSurface.copy(alpha = 0.22f)),
                            )
                        }
                        inner()
                    }
                },
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge, color = colors.onSurfaceVariant)
    }
}

private fun digitStyle(color: Color) = TextStyle(
    color = color,
    fontFamily = FontFamily.Monospace,
    fontWeight = FontWeight.Bold,
    fontSize = 30.sp,
    textAlign = TextAlign.Center,
)

@Composable
private fun Colon() {
    Box(Modifier.height(BoxSize).padding(horizontal = 4.dp), contentAlignment = Alignment.Center) {
        Text(":", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun PresetOption(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton)
            .padding(end = 8.dp),
    ) {
        RadioButton(selected = selected, onClick = null, modifier = Modifier.padding(8.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun VerticalRedLine() {
    Box(
        Modifier
            .padding(top = 48.dp)
            .width(2.dp)
            .height(BoxSize)
            .background(MaterialTheme.colorScheme.primary),
    )
}

@Composable
private fun HorizontalRedLine() {
    Box(
        Modifier
            .padding(vertical = 12.dp)
            .width(200.dp)
            .height(2.dp)
            .background(MaterialTheme.colorScheme.primary),
    )
}

// ---------- Datos extra ----------

@Composable
private fun ExtrasCard(result: CalcResult.Success) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    Card(
        modifier = Modifier.widthIn(max = 480.dp).fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                stringResource(R.string.more_data),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            Text(if (expanded) "▲" else "▼", color = MaterialTheme.colorScheme.primary)
        }
        AnimatedVisibility(visible = expanded) {
            Column(Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                ExtraRow(
                    stringResource(R.string.speed),
                    RaceCalculator.formatSpeed(RaceCalculator.speedKmh(result.exactPaceSecondsPerKm)),
                )
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                Text(
                    stringResource(R.string.estimates),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(4.dp))
                RaceCalculator.estimates(result.exactPaceSecondsPerKm).forEach { (preset, seconds) ->
                    ExtraRow(stringResource(preset.labelRes()), RaceCalculator.formatDuration(seconds))
                }
            }
        }
    }
}

@Composable
private fun ExtraRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(label, modifier = Modifier.weight(1f))
        Text(value, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
    }
}

private fun Preset.labelRes(): Int = when (this) {
    Preset.FIVE_K -> R.string.preset_5k
    Preset.TEN_K -> R.string.preset_10k
    Preset.HALF_MARATHON -> R.string.preset_half_marathon
    Preset.MARATHON -> R.string.preset_marathon
}
