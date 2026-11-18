package com.example

import com.example.engine.T9Engine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testTextToT9Conversion() {
        assertEquals("466453", T9Engine.textToT9("Google"))
        assertEquals("247663", T9Engine.textToT9("Chrome"))
        assertEquals("2288379", T9Engine.textToT9("Battery"))
    }

    @Test
    fun testExactPrefixMatch() {
        val result = T9Engine.matchT9("Google Chrome", "4664")
        assertTrue("Expected 4664 to match Google Chrome", result.isMatch)
        assertEquals(0 until 4, result.matchedRanges.first())
    }

    @Test
    fun testWordBoundaryMatch() {
        // "2476" corresponds to "CHRO" in Chrome
        val result = T9Engine.matchT9("Google Chrome", "2476")
        assertTrue("Expected 2476 to match Chrome in Google Chrome", result.isMatch)
    }

    @Test
    fun testAcronymMatch() {
        // 'G' (4) + 'M' (6) = "46" for "Google Maps"
        val result = T9Engine.matchT9("Google Maps", "46")
        assertTrue("Expected 46 to match acronym GM in Google Maps", result.isMatch)
    }

    @Test
    fun testPhoneNumberMatch() {
        val result = T9Engine.matchPhoneNumber("+1 (555) 234-5678", "555234")
        assertTrue("Expected 555234 to match formatted phone number", result.isMatch)

        val nonMatch = T9Engine.matchPhoneNumber("+1 (555) 234-5678", "99999")
        assertFalse(nonMatch.isMatch)
    }

    @Test
    fun testPredictiveTextSuggestions() {
        val suggestions = T9Engine.getPredictiveSuggestions("4664")
        assertTrue("Suggestions should contain GOOGLE for 4664", suggestions.contains("GOOGLE"))

        val batterySuggestions = T9Engine.getPredictiveSuggestions("2288")
        assertTrue("Suggestions should contain BATTERY for 2288", batterySuggestions.contains("BATTERY"))
    }
}
