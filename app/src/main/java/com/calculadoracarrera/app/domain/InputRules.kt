package com.calculadoracarrera.app.domain

/** Cada casilla de entrada, con su campo, nº de dígitos y valor máximo. */
enum class InputBox(val field: Field, val maxDigits: Int, val maxValue: Int) {
    DIST_KM(Field.DISTANCE, 3, 999),
    DIST_M(Field.DISTANCE, 3, 999),
    HOURS(Field.TIME, 2, 99),
    MINUTES(Field.TIME, 2, 59),
    SECONDS(Field.TIME, 2, 59),
    PACE_MIN(Field.PACE, 2, 59),
    PACE_SEC(Field.PACE, 2, 59),
}

object InputRules {
    /**
     * Limpia lo que escribe el usuario: solo dígitos, como mucho [InputBox.maxDigits]
     * (si se pasa, se quedan los últimos, como en un marcador digital) y sin
     * superar [InputBox.maxValue]. Si el valor no es válido, devuelve [old].
     */
    fun sanitize(old: String, raw: String, box: InputBox): String {
        var digits = raw.filter { it in '0'..'9' }
        if (digits.length > box.maxDigits) digits = digits.takeLast(box.maxDigits)
        if (digits.isNotEmpty() && digits.toInt() > box.maxValue) return old
        return digits
    }

    fun parse(value: String): Long = value.toLongOrNull() ?: 0L

    /** Rellena con ceros a la izquierda; los km se muestran sin relleno. */
    fun pad(value: Long, box: InputBox): String =
        if (box == InputBox.DIST_KM) value.toString()
        else value.toString().padStart(box.maxDigits, '0')
}
