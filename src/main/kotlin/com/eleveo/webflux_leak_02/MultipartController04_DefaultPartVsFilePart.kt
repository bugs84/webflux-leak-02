package com.eleveo.webflux_leak_02

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.codec.multipart.Part
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.nio.file.Paths

private val log = KotlinLogging.logger {}

@RestController
class MultipartController04_DefaultPartVsFilePart {

    var fileNumber = 1

    @Autowired
    private lateinit var env: Environment

//    @PostMapping("/upload-files04")

    @RequestMapping(
        method = [RequestMethod.POST],
        value = ["/upload-files04"],
        produces = ["application/json"],
        consumes = ["multipart/form-data"]
    )
    fun uploadFileWithoutEntity(@RequestPart("files", required = false) filePartFlux: Flux<Part>): Mono<*> {

        writeEnvironment()

        return filePartFlux.collectList().flatMap { fileList: List<Part> ->
            Flux.fromIterable(fileList).flatMapSequential { file: Part ->
                DataBufferUtils.write(file.content(), Paths.get("file_${fileNumber++}.mp3")).then(Mono.just("ok"))
            }.then(Mono.just("OK"))
        }
            .onErrorResume { error: Throwable? ->
                log.error(error) { "Error uploading files" }
                Mono.just("Error uploading files")
            }
    }

    private fun writeEnvironment() {
        val environment = env
        val appName = environment.getProperty("spring.application.name", "UNKNOWN")
        val leakDetection = environment.getProperty("spring.netty.leak-detection", "UNKNOWN")
        log.info { "appName: '$appName', leak-detection: '$leakDetection'" }
    }


}