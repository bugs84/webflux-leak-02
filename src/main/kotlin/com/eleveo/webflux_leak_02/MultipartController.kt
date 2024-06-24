package com.eleveo.webflux_leak_02

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.nio.file.Paths

private val log = KotlinLogging.logger {}

var fileNumber = 1

@RestController
class MultipartController {

    @PostMapping("/upload-files")
    fun uploadFileWithoutEntity(@RequestPart("files") filePartFlux: Flux<FilePart>): Mono<*> {
        return filePartFlux.flatMap { file: FilePart ->
            file.transferTo(
                Paths.get(file.filename() + fileNumber++)
            )
        }
            .then(Mono.just("OK"))
            .onErrorResume { error: Throwable? ->
                log.error(error) { "Error uploading files" }
                Mono.just("Error uploading files")
            }
    }

}