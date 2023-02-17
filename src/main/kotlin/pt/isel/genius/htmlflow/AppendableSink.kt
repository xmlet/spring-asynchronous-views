package pt.isel.genius.htmlflow

import reactor.core.publisher.Flux
import reactor.core.publisher.Sinks
import reactor.core.publisher.Sinks.EmitFailureHandler


class AppendableSink : Appendable, AutoCloseable {
    private val sink = Sinks.many().replay().all<String>()

    override fun close() {
        sink.emitComplete(EmitFailureHandler.FAIL_FAST)
    }

    fun asFLux(): Flux<String> {
        return sink.asFlux()
    }
    override fun append(csq: CharSequence): Appendable {
        sink.emitNext(csq.toString(), EmitFailureHandler.FAIL_FAST)
        return this
    }

    override fun append(csq: CharSequence, start: Int, end: Int): Appendable {
        append(csq.subSequence(start, end))
        return this
    }

    override fun append(c: Char): Appendable {
        append(c.toString())
        return this
    }
}
