package pt.isel.genius.htmlflow

import reactor.core.publisher.Flux
import reactor.core.publisher.Sinks
import reactor.core.publisher.Sinks.EmitFailureHandler
import java.io.IOException
import java.io.OutputStream
import java.io.PrintStream


class PrintStreamSink : PrintStream(NullOutputStream()) {
    private val sink = Sinks.many().replay().all<String>()
    override fun write(buf: ByteArray, off: Int, len: Int) {
        sink.emitNext(String(buf, off, len), EmitFailureHandler.FAIL_FAST)
    }

    override fun close() {
        sink.emitComplete(EmitFailureHandler.FAIL_FAST)
    }

    fun asFLux(): Flux<String> {
        return sink.asFlux()
    }

    internal class NullOutputStream : OutputStream() {
        @Throws(IOException::class)
        override fun write(b: Int) {
            throw UnsupportedOperationException()
        }
    }
}
