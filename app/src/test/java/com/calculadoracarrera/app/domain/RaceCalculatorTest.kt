package com.calculadoracarrera.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RaceCalculatorTest {

    private fun success(r: CalcResult): CalcResult.Success {
        assertTrue("Se esperaba Success y fue $r", r is CalcResult.Success)
        return r as CalcResult.Success
    }

    private fun sec(h: Long, m: Long, s: Long) = RaceCalculator.hmsToSeconds(h, m, s)

    @Test
    fun `10 km en 50 minutos da 5 00 por km`() {
        val r = success(RaceCalculator.calculate(10.0, sec(0, 50, 0), 0))
        assertEquals(Field.PACE, r.calculated)
        assertEquals(MinSec(5, 0), RaceCalculator.toMinSec(r.paceSeconds))
    }

    @Test
    fun `maraton a 5 00 por km da 3 30 59`() {
        val r = success(RaceCalculator.calculate(Preset.MARATHON.km, 0, 300))
        assertEquals(Field.TIME, r.calculated)
        assertEquals(Hms(3, 30, 59), RaceCalculator.toHms(r.timeSeconds))
    }

    @Test
    fun `media maraton en 1 45 00 da 4 59 por km`() {
        val r = success(RaceCalculator.calculate(Preset.HALF_MARATHON.km, sec(1, 45, 0), 0))
        assertEquals(MinSec(4, 59), RaceCalculator.toMinSec(r.paceSeconds))
    }

    @Test
    fun `5 30 por km durante 1 hora da 10 909 km`() {
        val r = success(RaceCalculator.calculate(0.0, sec(1, 0, 0), 330))
        assertEquals(Field.DISTANCE, r.calculated)
        assertEquals(10.909, r.distanceKm, 1e-9)
        assertEquals(KmM(10, 909), RaceCalculator.toKmM(r.distanceKm))
    }

    @Test
    fun `media maraton se muestra como 21 km 098 m`() {
        assertEquals(KmM(21, 98), RaceCalculator.toKmM(Preset.HALF_MARATHON.km))
        assertEquals("098", InputRules.pad(98, InputBox.DIST_M))
    }

    @Test
    fun `los desbordes de segundos y minutos se normalizan`() {
        assertEquals(Hms(1, 0, 0), RaceCalculator.toHms(3600))
        assertEquals(Hms(0, 1, 0), RaceCalculator.toHms(60))
        assertEquals(KmM(1, 0), RaceCalculator.toKmM(0.9996))
    }

    @Test
    fun `con menos de dos campos avisa`() {
        assertEquals(
            CalcResult.Failure(CalcError.NOT_ENOUGH_FIELDS),
            RaceCalculator.calculate(10.0, 0, 0),
        )
        assertEquals(
            CalcResult.Failure(CalcError.NOT_ENOUGH_FIELDS),
            RaceCalculator.calculate(0.0, 0, 0),
        )
    }

    @Test
    fun `con tres campos y sin calculo previo pide dejar uno vacio`() {
        assertEquals(
            CalcResult.Failure(CalcError.ALL_FIELDS_FILLED),
            RaceCalculator.calculate(10.0, 3000, 300),
        )
    }

    @Test
    fun `con tres campos recalcula el ultimo calculado`() {
        // El usuario cambia el tiempo a 45:00 y el ritmo se recalcula.
        val r = success(RaceCalculator.calculate(10.0, sec(0, 45, 0), 300, Field.PACE))
        assertEquals(Field.PACE, r.calculated)
        assertEquals(270L, r.paceSeconds)
    }

    @Test
    fun `ritmo fuera de rango da error`() {
        assertEquals(
            CalcResult.Failure(CalcError.PACE_OUT_OF_RANGE),
            RaceCalculator.calculate(10.0, sec(0, 10, 0), 0), // 1:00 /km
        )
        assertEquals(
            CalcResult.Failure(CalcError.PACE_OUT_OF_RANGE),
            RaceCalculator.calculate(1.0, sec(0, 31, 0), 0), // 31:00 /km
        )
    }

    @Test
    fun `tiempo mayor de 99 59 59 da error`() {
        assertEquals(
            CalcResult.Failure(CalcError.TIME_OUT_OF_RANGE),
            RaceCalculator.calculate(999.0, 0, 1800),
        )
    }

    @Test
    fun `velocidad y estimaciones`() {
        assertEquals(12.0, RaceCalculator.speedKmh(300.0), 1e-9)
        val est = RaceCalculator.estimates(300.0)
        assertEquals(1500L, est[Preset.FIVE_K])
        assertEquals(3000L, est[Preset.TEN_K])
        assertEquals("3:30:59", RaceCalculator.formatDuration(est.getValue(Preset.MARATHON)))
    }

    @Test
    fun `formatos`() {
        assertEquals("4:59 /km", RaceCalculator.formatPace(299))
        assertEquals("25:00", RaceCalculator.formatDuration(1500))
        assertEquals("12,00 km/h", RaceCalculator.formatSpeed(12.0))
    }
}
