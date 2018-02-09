package ch.tutteli.atrium.api.cc.en_UK.assertions.iterable.contains.builders

import ch.tutteli.atrium.creating.iterable.contains.builders.IterableContainsBuilder
import ch.tutteli.atrium.creating.iterable.contains.searchbehaviours.IterableContainsInAnyOrderSearchBehaviour

/**
 * Represents the builder of a `contains at least once but at most` check within the fluent API of a
 * sophisticated `contains` assertion for [Iterable].
 *
 * @param T The input type of the search.
 *
 * @constructor Represents the builder of a `contains at least once but at most` check within the fluent API of a
 *   sophisticated `contains` assertion for [Iterable].
 * @param times The number which the check will compare against the actual number of times an expected entry is
 *   found in the [Iterable].
 * @param containsBuilder The previously used [IterableContainsBuilder].
 */
@Deprecated("use the builder from the package creating, will be removed with 1.0.0", ReplaceWith("ch.tutteli.atrium.api.cc.en_UK.creating.iterable.contains.builders.IterableContainsAtMostCheckerBuilder"))
open class IterableContainsAtMostCheckerBuilder<out E, out T : Iterable<E>>(
    times: Int,
    containsBuilder: IterableContainsBuilder<E, T, IterableContainsInAnyOrderSearchBehaviour>
) : ch.tutteli.atrium.api.cc.en_UK.creating.iterable.contains.builders.IterableContainsAtMostCheckerBuilder<E, T>(
    times,
    containsBuilder
)
