package com.kropotov.lovehatebackend.utilities

import java.util.*
import kotlin.math.min

/**
 * Calculates the similarity (a number within 0 and 1) between two strings.
 */
object StringSimilarity {

    fun similarity(s1: String, s2: String): Double {
        var (longer, shorter) = s1 to s2
        if (s1.length < s2.length) { // longer should always have greater length
            longer = s2; shorter = s1
        }
        val longerLength = longer.length
        if (longerLength == 0) {
            return 1.0; /* both strings are zero length */
        }

        return (longerLength - editDistance(longer, shorter)) / longerLength.toDouble();
    }

    private fun editDistance(string1: String, string2: String): Int {
        val s1 = string1.lowercase(Locale.getDefault())
        val s2 = string2.lowercase(Locale.getDefault())
        val costs = IntArray(s2.length + 1)
        for (i in 0..s1.length) {
            var lastValue = i
            for (j in 0..s2.length) {
                if (i == 0) costs[j] = j else {
                    if (j > 0) {
                        var newValue = costs[j - 1]
                        if (s1[i - 1] != s2[j - 1]) newValue =
                            (min(
                                min(newValue.toDouble(), lastValue.toDouble()),
                                costs[j].toDouble()
                            ) + 1).toInt()
                        costs[j - 1] = lastValue
                        lastValue = newValue
                    }
                }
            }
            if (i > 0) costs[s2.length] = lastValue
        }
        return costs[s2.length]
    }
}