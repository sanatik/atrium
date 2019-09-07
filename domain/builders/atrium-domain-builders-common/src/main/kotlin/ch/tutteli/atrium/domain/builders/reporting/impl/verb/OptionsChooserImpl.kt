package ch.tutteli.atrium.domain.builders.reporting.impl.verb

import ch.tutteli.atrium.core.None
import ch.tutteli.atrium.core.Option
import ch.tutteli.atrium.core.Some
import ch.tutteli.atrium.domain.builders.reporting.ExpectBuilder
import ch.tutteli.atrium.domain.builders.reporting.ExpectOptions
import ch.tutteli.atrium.reporting.translating.Translatable

class OptionsChooserImpl : ExpectBuilder.OptionsChooser {

    private var description: Option<Translatable> = None
    private var representation: Option<Any?> = None
    private var nullRepresentation: Option<Any> = None

    override fun withVerb(verb: Translatable) {
        this.description = Some(verb)
    }

    override fun withRepresentation(representation: Any?) {
        this.representation = Some(representation)
    }

    override fun withNullRepresentation(nullRepresentation: Any) {
        this.nullRepresentation = Some(nullRepresentation)
    }

    fun build() = ExpectOptions(description, representation, nullRepresentation)
}
