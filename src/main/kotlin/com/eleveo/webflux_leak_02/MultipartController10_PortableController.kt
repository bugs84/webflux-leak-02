package com.eleveo.webflux_leak_02

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.codec.multipart.Part
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.nio.file.Paths

private val log = KotlinLogging.logger {}

@RestController
class MultipartController10_PortableController {

    var fileNumber = 1

    @PostMapping("/upload-files10")
    fun uploadFileWithoutEntity(@RequestPart("files", required = false) filePartFlux: Flux<Part>): Mono<*> {
        return filePartFlux.collectList().flatMap { fileList: List<Part> ->
            Flux.fromIterable(fileList).flatMapSequential { file: Part ->
                val destination = Paths.get("generated_${fileNumber++}.file")
                log.info { "File is going to be saved into '$destination'" }
                DataBufferUtils.write(file.content(), destination).then(Mono.just("ok"))
            }.then(Mono.just("OK"))
        }
            .onErrorResume { error: Throwable ->
                log.error(error) { "Error uploading files" }
                throw error
            }
    }




}