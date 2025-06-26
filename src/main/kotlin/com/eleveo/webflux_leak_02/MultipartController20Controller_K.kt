package com.eleveo.webflux_leak_02

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE
import org.springframework.http.codec.multipart.FilePartEvent
import org.springframework.http.codec.multipart.FormPartEvent
import org.springframework.http.codec.multipart.PartEvent
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.nio.file.Path
import java.nio.file.Paths

private val log = KotlinLogging.logger {}

@RestController
class MultipartController20Controller {

    var fileNumber = 1

    @PostMapping(
        "/upload-files20",
        consumes = [MULTIPART_FORM_DATA_VALUE],
        produces = [APPLICATION_JSON_VALUE]
    )
    fun uploadFileWithoutEntity(@RequestPart("files") filePartFlux: Flux<PartEvent>): Mono<String> {
        return filePartFlux
            .windowUntil(PartEvent::isLast)
            .flatMap { window ->
                window.switchOnFirst { signal, flux ->
                    if (signal.hasValue()) {
                        val event = signal.get()
                        when (event) {
                            is FilePartEvent -> {
                                val filename = event.filename()
                                val destination = Paths.get("generated_${fileNumber++}.file")
                                log.info { "Processing file: $filename to $destination" }
                                DataBufferUtils.write(flux.map(PartEvent::content), destination)
                                    .then(Mono.just("File processed: $filename"))
                            }
                            is FormPartEvent -> {
                                Mono.just("Form field: ${event.value()}")
                            }
                            else -> Mono.error(IllegalStateException("Unexpected event type: ${event?.javaClass}"))
                        }
                    } else {
                        Mono.error(IllegalStateException("No signal value"))
                    }
                }
            }
            .collectList()
            .map { results -> "Processed: ${results.joinToString()}" }
            .onErrorResume { error ->
                log.error(error) { "Error processing upload" }
                Mono.just("Error: ${error.message}")
            }
    }
}
