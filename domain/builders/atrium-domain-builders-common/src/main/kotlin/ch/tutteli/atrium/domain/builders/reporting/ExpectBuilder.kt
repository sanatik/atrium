package ch.tutteli.atrium.domain.builders.reporting

import ch.tutteli.atrium.core.None
import ch.tutteli.atrium.core.Option
import ch.tutteli.atrium.core.Some
import ch.tutteli.atrium.core.getOrElse
import ch.tutteli.atrium.creating.Expect
import ch.tutteli.atrium.domain.builders.reporting.impl.verb.*
import ch.tutteli.atrium.reporting.RawString
import ch.tutteli.atrium.reporting.Reporter
import ch.tutteli.atrium.reporting.ReporterFactory
import ch.tutteli.atrium.reporting.reporter
import ch.tutteli.atrium.reporting.translating.Translatable
import ch.tutteli.atrium.reporting.translating.Untranslatable

/**
 * Defines the contract to create custom assertion verbs.
 */
interface ExpectBuilder {
    companion object {

        /**
         * Entry point to use the [ExpectBuilder] which helps in creating
         * an assertion verb for the given [subject] or in other words an [Expect] for the given [subject].
         */
        fun <T> createForSubject(subject: T): AssertionVerbOption<T> = AssertionVerbOptionImpl(Some(subject))
    }

    /**
     * Option step which allows to specify the assertion verb which shall be used.
     *
     * @param T the type of the subject.
     */
    interface AssertionVerbOption<T> {
        /**
         * The previously specified subject of the assertion.
         */
        val maybeSubject: Option<T>

        /**
         * Wraps the given [verb] into an [Untranslatable] and uses it as assertion verb.
         */
        fun withVerb(verb: String) = withVerb(Untranslatable(verb))

        /**
         * Uses the given [verb] as assertion verb.
         */
        fun withVerb(verb: Translatable): ReporterOption<T>
    }

    /**
     * Option step which allows to specify the [Reporter] which shall be used during reporting
     *
     * @param T the type of the subject.
     */
    interface ReporterOption<T> {
        /**
         * The previously specified subject of the assertion.
         */
        val maybeSubject: Option<T>

        /**
         * The previously defined assertion verb.
         */
        val assertionVerb: Translatable

        /**
         * Uses the [Reporter] returned by [reporter] for reporting.
         *
         * You can configure the default [Reporter] via [ReporterFactory.ATRIUM_PROPERTY_KEY],
         * see [reporter] for more details.
         */
        fun withDefaultReporter() = withCustomReporter(reporter)

        /**
         * Uses the given [reporter] for reporting.
         */
        fun withCustomReporter(reporter: Reporter): OptionsStep<T>

        companion object {
            fun <T> create(
                maybeSubject: Option<T>,
                assertionVerb: Translatable
            ): ReporterOption<T> = ReporterOptionImpl(maybeSubject, assertionVerb)
        }
    }

    interface OptionsStep<T> {
        /**
         * The previously specified subject of the assertion.
         */
        val maybeSubject: Option<T>

        /**
         * The previously defined assertion verb.
         */
        val assertionVerb: Translatable

        /**
         * The previously specified [Reporter]
         */
        val reporter: Reporter

        fun withOptions(configuration: OptionsChooser.() -> Unit): FinalStep<T> =
            withOptions(ExpectOptions(configuration))

        fun withOptions(options: ExpectOptions): FinalStep<T>

        /**
         * Creates a new [Expect] based on the previously defined mandatory maybeOptions but without any optional maybeOptions.
         *
         * Use [withOptions] if you want to define optional [ExpectOptions] such as, override the
         * verb or define an own representation.
         */
        fun build() = withOptions(ExpectOptions()).build()

        companion object {
            fun <T> create(
                maybeSubject: Option<T>,
                assertionVerb: Translatable,
                reporter: Reporter
            ): OptionsStep<T> = OptionsStepImpl(maybeSubject, assertionVerb, reporter)
        }
    }

    interface OptionsChooser {

        fun withVerb(verb: String) = withVerb(Untranslatable(verb))

        fun withVerb(verb: Translatable)

        fun withRepresentation(representation: Any?)

        fun withNullRepresentation(translatable: Translatable) =
            withNullRepresentation(RawString.create(translatable))

        fun withNullRepresentation(nullRepresentation: Any)

        companion object {
            fun createAndBuild(configuration: OptionsChooser.() -> Unit): ExpectOptions =
                OptionsChooserImpl().apply(configuration).build()
        }
    }

    /**
     * Final step in the assertion verb building process, creates a new [Expect] based on the so far specified maybeOptions.
     *
     * @param T the type of the subject.
     */
    interface FinalStep<T> {
        /**
         * The previously specified subject of the assertion.
         */
        val maybeSubject: Option<T>

        /**
         * The previously defined assertion verb.
         */
        val assertionVerb: Translatable

        /**
         * The previously specified [Reporter]
         */
        val reporter: Reporter

        /**
         * The previously specified [ExpectOptions].
         */
        val options: ExpectOptions

        /**
         * Creates a new [Expect] based on the previously defined maybeOptions.
         */
        fun build(): Expect<T>

        companion object {
            fun <T> create(
                maybeSubject: Option<T>,
                assertionVerb: Translatable,
                reporter: Reporter,
                options: ExpectOptions
            ): FinalStep<T> = FinalStepImpl(maybeSubject, assertionVerb, reporter, options)
        }
    }
}

/**
 * Additional options for the [ExpectBuilder] to create an [Expect].
 */
data class ExpectOptions(
    val assertionVerb: Translatable? = null,
    val representation: Any? = null,
    val nullRepresentation: Any? = null
) {
    /**
     * Merges the given [options] with this object creating a new [ExpectOptions]
     * where defined properties in [options] will have precedence over properties defined in this instance.
     *
     * For instance, this object has defined [representation] (meaning it is [Some]) and the given [options] as well,
     * then the resulting [ExpectOptions] will have the [representation] of [options].
     */
    fun merge(options: ExpectOptions): ExpectOptions =
        ExpectOptions(
            options.assertionVerb ?: assertionVerb,
            options.representation ?: representation,
            options.nullRepresentation ?: nullRepresentation
        )

//    companion object {
//        operator fun invoke(configuration: ExpectBuilder.OptionsChooser.() -> Unit): ExpectOptions =
//            ExpectBuilder.OptionsChooser.createAndBuild(configuration)
//    }
}

fun ExpectOptions(configuration: ExpectBuilder.OptionsChooser.() -> Unit): ExpectOptions =
        ExpectBuilder.OptionsChooser.createAndBuild(configuration)
