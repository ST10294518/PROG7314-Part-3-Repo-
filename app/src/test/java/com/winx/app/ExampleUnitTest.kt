package com.winx.app

import org.junit.Test

import org.junit.Assert.*
import com.winx.app.utils.translate

/**
 * Local unit tests, run on the development machine (host) — no emulator needed.
 * These are executed automatically by GitHub Actions on every push.
 */
class ExampleUnitTest {

    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun translate_returnsZuluTranslation_whenLanguageIsZulu() {
        val result = translate("Settings", "isiZulu (Zulu)")
        assertEquals("Izilungiselelo", result)
    }

    @Test
    fun translate_returnsOriginalText_whenLanguageIsUnknown() {
        val result = translate("Settings", "Klingon")
        assertEquals("Settings", result)
    }
}
