package com.eleveo.webflux_leak_02.sample05_flux

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE
import org.springframework.http.codec.multipart.PartEvent
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

private val log = KotlinLogging.logger {}

@RestController
class UploadControllerSimple1 {

    @PostMapping("/sample05", consumes = [MULTIPART_FORM_DATA_VALUE], produces = [APPLICATION_JSON_VALUE])
    fun upload(@RequestBody allParts: Flux<PartEvent>): Mono<String> {
        return allParts.doOnNext {
            log.info { "buffer size: ${it.content().readableByteCount()}" }
            DataBufferUtils.release(it.content())
        }.then(Mono.just("Response text"))
    }
}


