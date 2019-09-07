package ch.tutteli.atrium.domain.builders.reporting.impl.verb

import ch.tutteli.atrium.core.Option
import ch.tutteli.atrium.core.coreFactory
import ch.tutteli.atrium.core.getOrElse
import ch.tutteli.atrium.creating.Expect
import ch.tutteli.atrium.creating.ReportingAssertionContainer
import ch.tutteli.atrium.domain.builders.reporting.ExpectBuilder
import ch.tutteli.atrium.domain.builders.reporting.ExpectOptions
import ch.tutteli.atrium.reporting.RawString
import ch.tutteli.atrium.reporting.Reporter
import ch.tutteli.atrium.reporting.SHOULD_NOT_BE_SHOWN_TO_THE_USER_BUG
import ch.tutteli.atrium.reporting.translating.Translatable

class AssertionVerbOptionImpl<T>(override val maybeSubject: Option<T>) : ExpectBuilder.AssertionVerbOption<T> {
    override fun withVerb(verb: Translatable): ExpectBuilder.ReporterOption<T> =
        ExpectBuilder.ReporterOption.create(maybeSubject, verb)
}

class ReporterOptionImpl<T>(
    override val maybeSubject: Option<T>,
    override val assertionVerb: Translatable
) : ExpectBuilder.ReporterOption<T> {

    override fun withCustomReporter(reporter: Reporter): ExpectBuilder.OptionsStep<T> =
        ExpectBuilder.OptionsStep.create(maybeSubject, assertionVerb, reporter)
}

class OptionsStepImpl<T>(
    override val maybeSubject: Option<T>,
    override val assertionVerb: Translatable,
    override val reporter: Reporter
) : ExpectBuilder.OptionsStep<T> {

    override fun withOptions(options: ExpectOptions): ExpectBuilder.FinalStep<T> =
        ExpectBuilder.FinalStep.create(maybeSubject, assertionVerb, reporter, options)
}

class FinalStepImpl<T>(
    override val maybeSubject: Option<T>,
    override val assertionVerb: Translatable,
    override val reporter: Reporter,
    override val options: ExpectOptions
) : ExpectBuilder.FinalStep<T> {

    override fun build(): Expect<T> =
        coreFactory.newReportingAssertionContainer(
            ReportingAssertionContainer.AssertionCheckerDecorator.create(
                options.assertionVerb.getOrElse { assertionVerb },
                maybeSubject,
                options.representation.getOrElse {
                    maybeSubject.getOrElse { RawString.create(SHOULD_NOT_BE_SHOWN_TO_THE_USER_BUG) }
                },
                coreFactory.newThrowingAssertionChecker(reporter),
                options.nullRepresentation.getOrElse { RawString.NULL }
            )
        )
}
