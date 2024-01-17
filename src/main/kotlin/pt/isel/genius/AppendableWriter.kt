package pt.isel.genius

import reactor.core.publisher.Flux
import reactor.core.publisher.Sinks
import reactor.core.publisher.Sinks.EmitFailureHandler
import java.io.Writer


class AppendableWriter(block: AppendableWriter.() -> Unit) : Writer(), AutoCloseable {
    private val sink = Sinks.many().replay().all<String>()

    init {
        this.block()
    }

    override fun close() {
        sink.emitComplete(EmitFailureHandler.FAIL_FAST)
    }

    override fun flush() { }

    override fun write(cbuf: CharArray, off: Int, len: Int) {
        sink.emitNext(String(cbuf, off, len), EmitFailureHandler.FAIL_FAST)
    }

    fun asFlux(): Flux<String> {
        return sink.asFlux()
    }

}
