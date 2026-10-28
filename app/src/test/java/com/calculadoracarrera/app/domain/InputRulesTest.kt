package com.calculadoracarrera.app.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class InputRulesTest {

    @Test
    fun `solo admite digitos`() {
        assertEquals("12", InputRules.sanitize("", "1-2,", InputBox.MINUTES))
    }

    @Test
    fun `rechaza valores por encima del maximo`() {
        assertEquals("5", InputRules.sanitize("5", "75", InputBox.MINUTES))
        assertEquals("", InputRules.sanitize("", "60", InputBox.SECONDS))
    }

    @Test
    fun `al pasarse de digitos se queda con los ultimos`() {
        assertEquals("53", InputRules.sanitize("05", "053", InputBox.MINUTES))
        assertEquals("981", InputRules.sanitize("098", "0981", InputBox.DIST_M))
    }

    @Test
    fun `campo vacio es valido`() {
        assertEquals("", InputRules.sanitize("12", "", InputBox.HOURS))
        assertEquals(0L, InputRules.parse(""))
    }

    @Test
    fun `relleno con ceros`() {
        assertEquals("05", InputRules.pad(5, InputBox.SECONDS))
        assertEquals("21", InputRules.pad(21, InputBox.DIST_KM))
        assertEquals("000", InputRules.pad(0, InputBox.DIST_M))
    }
}
