package ch.tutteli.atrium.core.robstoll.lib.reporting

import ch.tutteli.atrium.api.fluent.en_GB.*
import ch.tutteli.atrium.api.verbs.internal.expect
import ch.tutteli.atrium.core.polyfills.stackBacktrace
import ch.tutteli.atrium.creating.Expect
import ch.tutteli.atrium.domain.builders.AssertImpl
import ch.tutteli.atrium.domain.builders.ExpectImpl
import ch.tutteli.atrium.domain.builders.reporting.ExpectOptions
import ch.tutteli.atrium.domain.builders.utils.subExpect
import ch.tutteli.atrium.reporting.AtriumErrorAdjuster
import ch.tutteli.atrium.reporting.reporter
import ch.tutteli.atrium.verbs.internal.AssertionVerb
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

class AdjustStackSpec : Spek({

    describe("no-op adjuster") {
        fun <T : Any> assertNoOp(subject: T) = createExpect(
            subject, AssertImpl.coreFactory.newNoOpAtriumErrorAdjuster()
        )

        it("contains spek, junit, atrium.creating and atrium.reporting") {
            expect {
                assertNoOp(1).toBe(2)
            }.toThrow<AssertionError> {
                feature { f(it::stackBacktrace) }.contains(
                    { startsWith("org.spekframework.spek2") },
                    { startsWith("ch.tutteli.atrium.creating") },
                    { startsWith("ch.tutteli.atrium.reporting") }
                )
            }
        }
    }

    fun mapStartsWith(list: List<String>): Pair<Expect<String>.() -> Unit, Array<out Expect<String>.() -> Unit>> {
        val asserts = list.map { c -> subExpect<String> { startsWith(c) } }
        return asserts.first() to asserts.drop(1).toTypedArray()
    }

    mapOf(
        "remove test runner adjuster" to Triple(
            ExpectImpl.coreFactory.newRemoveRunnerAtriumErrorAdjuster(),
            listOf("org.spekframework.spek2", "kotlin.coroutines", "kotlinx.coroutines"),
            listOf("ch.tutteli.atrium")
        ),
        "remove atrium adjuster" to Triple(
            ExpectImpl.coreFactory.newRemoveAtriumFromAtriumErrorAdjuster(),
            listOf("ch.tutteli.atrium"),
            listOf("org.spekframework.spek2")
        )
    ).forEach { (description, triple) ->
        val (adjuster, containsNot, contains) = triple
        val (containsNotFirst, containsNotRest) = mapStartsWith(containsNot)
        val (containsFirst, containsRest) = mapStartsWith(contains)
        describe(description) {
            it("does not contain $containsNot in stackBacktrace but $contains") {
                expect {
                    createExpect(1, adjuster).toBe(2)
                }.toThrow<AssertionError> {
                    feature { f(it::stackBacktrace) }
                        .containsNot.entries(containsNotFirst, *containsNotRest)
                        .contains(containsFirst, *containsRest)
                }
            }


            it("does not contain $containsNot in stackBacktrace of cause, but $contains") {
                val throwable = IllegalArgumentException("hello", UnsupportedOperationException("world"))
                adjuster.adjust(throwable)
                expect(throwable.cause!!.stackBacktrace)
                    .containsNot.entries(containsNotFirst, *containsNotRest)
                    .contains(containsFirst, *containsRest)
            }

            it("does not contain $containsNot in stackBacktrace of cause of cause, but $contains") {
                val throwable = IllegalArgumentException(
                    "hello",
                    UnsupportedOperationException("world", IllegalStateException("and good night"))
                )
                adjuster.adjust(throwable)
                expect(throwable.cause!!.cause!!.stackBacktrace)
                    .containsNot.entries(containsNotFirst, *containsNotRest)
                    .contains(containsFirst, *containsRest)
            }

            it("does not contain $containsNot in stackBacktrace of suppressed exception, but $contains") {
                val throwable1 = IllegalArgumentException("hello", UnsupportedOperationException("world"))
                val throwable2 = IllegalArgumentException("hello", UnsupportedOperationException("world"))
                val throwable = IllegalStateException("with suppressed")
                throwable.addSuppressed(throwable1)
                throwable.addSuppressed(throwable2)
                adjuster.adjust(throwable)
                expect(throwable.suppressed).asIterable().all {
                    feature { f(it::stackBacktrace) }
                        .containsNot.entries(containsNotFirst, *containsNotRest)
                        .contains(containsFirst, *containsRest)
                }
            }

            it("does not contain $containsNot in stackBacktrace of cause of suppressed exception, but $contains") {
                val throwable1 = IllegalArgumentException("hello", UnsupportedOperationException("world"))
                val throwable2 = IllegalArgumentException("hello", UnsupportedOperationException("world"))
                val throwable = IllegalStateException("with suppressed")
                throwable.addSuppressed(throwable1)
                throwable.addSuppressed(throwable2)
                adjuster.adjust(throwable)
                expect(throwable.suppressed).asIterable().all {
                    //TODO #31 replace with shortcut fun
                    feature { f(it::cause) }.notToBeNull {
                        feature { f(it::stackBacktrace) }
                            .containsNot.entries(containsNotFirst, *containsNotRest)
                            .contains(containsFirst, *containsRest)
                    }
                }
            }
        }
    }

    mapOf(
        "combine remove runner adjuster and remove atrium adjuster" to
            ExpectImpl.coreFactory.newMultiAtriumErrorAdjuster(
                ExpectImpl.coreFactory.newRemoveRunnerAtriumErrorAdjuster(),
                ExpectImpl.coreFactory.newRemoveAtriumFromAtriumErrorAdjuster(),
                listOf()
            ),
        "combine remove atrium adjuster and remove runner adjuster" to
            ExpectImpl.coreFactory.newMultiAtriumErrorAdjuster(
                ExpectImpl.coreFactory.newRemoveAtriumFromAtriumErrorAdjuster(),
                ExpectImpl.coreFactory.newRemoveRunnerAtriumErrorAdjuster(),
                listOf()
            ),
        "combine noop ajdust, remove atrium adjuster and remove runner adjuster" to
            ExpectImpl.coreFactory.newMultiAtriumErrorAdjuster(
                ExpectImpl.coreFactory.newNoOpAtriumErrorAdjuster(),
                ExpectImpl.coreFactory.newRemoveAtriumFromAtriumErrorAdjuster(),
                listOf(ExpectImpl.coreFactory.newRemoveRunnerAtriumErrorAdjuster())
            ),
        "combine remove atrium adjuster, remove runner adjuster and noop adjuster several times" to
            ExpectImpl.coreFactory.newMultiAtriumErrorAdjuster(
                ExpectImpl.coreFactory.newRemoveAtriumFromAtriumErrorAdjuster(),
                ExpectImpl.coreFactory.newRemoveRunnerAtriumErrorAdjuster(),
                listOf(
                    ExpectImpl.coreFactory.newNoOpAtriumErrorAdjuster(),
                    ExpectImpl.coreFactory.newRemoveRunnerAtriumErrorAdjuster(),
                    ExpectImpl.coreFactory.newNoOpAtriumErrorAdjuster()
                )
            )
    ).forEach { (description, adjuster) ->
        describe(description) {
            it("stackBacktrace is empty as we filter out everything") {
                expect {
                    createExpect(1, adjuster).toBe(2)
                }.toThrow<AssertionError> {
                    feature { f(it::stackBacktrace) }.isEmpty()
                }
            }

            it("stackBacktrace of cause is empty as we filter out everything") {
                val throwable = IllegalArgumentException("hello", UnsupportedOperationException("world"))
                adjuster.adjust(throwable)
                expect(throwable.cause!!.stackBacktrace).isEmpty()
            }

            it("stackBacktrace of suppressed is empty as we filter out everything") {
                val throwable1 = IllegalArgumentException("hello", UnsupportedOperationException("world"))
                val throwable2 = IllegalArgumentException("hello", UnsupportedOperationException("world"))
                val throwable = IllegalStateException("with suppressed")
                throwable.addSuppressed(throwable1)
                throwable.addSuppressed(throwable2)
                adjuster.adjust(throwable)
                expect(throwable.suppressed).asIterable().all {
                    feature { f(it::stackBacktrace) }.isEmpty()
                }
            }
        }
    }
})

private fun <T : Any> createExpect(subject: T, adjuster: AtriumErrorAdjuster) =
    ExpectImpl.assertionVerbBuilder(subject)
        .withVerb(AssertionVerb.ASSERT)
        .withOptions(ExpectOptions(reporter = DelegatingReporter(reporter, adjuster)))
        .build()
