package ch.tutteli.atrium.api.cc.infix.en_UK

import ch.tutteli.atrium.creating.Assert
import ch.tutteli.atrium.reporting.translating.Translatable

/**
 * Method object to express `vararg Translatable` in the infix-api.
 */
class DefaultTranslationsOf(val expected: Translatable, vararg val otherExpected: Translatable)

/**
 * Method object to express `vararg ((Assert<T>) -> Unit)?` in the infix-api.
 */
class Entries<in T : Any, out A : ((Assert<T>) -> Unit)?>(val assertionCreator: A, vararg val otherAssertionCreators: A)

/**
 * Method object to express `vararg T` in the infix-api.
 */
class Objects<out T>(val expected: T, vararg val otherExpected: T) {
    constructor(values: Values<T>) : this(values.expected, *values.otherExpected)
}

/**
 * Method object to express `vararg String` in the infix-api.
 */
class RegexPatterns(val pattern: String, vararg val otherPatterns: String)

/**
 * Method object to express `vararg T` in the infix-api.
 */
class Values<out T>(val expected: T, vararg val otherExpected: T)
