package com.eleveo.webflux_leak_02

import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE
import org.springframework.http.codec.multipart.FilePartEvent
import org.springframework.http.codec.multipart.FormPartEvent
import org.springframework.http.codec.multipart.PartEvent
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

data class UploadResponse(val description: String, val fileName: String, val fileSize: Long)

@RestController
class UploadController {


    @PostMapping("/upload", consumes = [MULTIPART_FORM_DATA_VALUE], produces = [APPLICATION_JSON_VALUE])
    fun upload(@RequestBody allParts: Flux<PartEvent>): Mono<UploadResponse> {
        return allParts
            .windowUntil(PartEvent::isLast)
            .concatMap { partFlux ->
                partFlux.switchOnFirst { signal, events ->
                    if (signal.hasValue()) {
                        when (val evt = signal.get()) {
                            is FormPartEvent -> {
                                Mono.just("desc:${evt.value()}")
                            }
                            is FilePartEvent -> {
                                val filename = evt.filename()
                                DataBufferUtils.join(events.map(PartEvent::content))
                                    .map { db ->
                                        val bytes = ByteArray(db.readableByteCount())
                                        db.read(bytes)
                                        DataBufferUtils.release(db)
                                        "file:$filename:${bytes.size}"
                                    }
                            }
                            else -> Mono.error<String>(RuntimeException("Unexpected event: $evt"))
                        }
                    } else {
                        Mono.empty()
                    }
                }
            }
            .collectList()
            .map { list ->
                var desc = ""
                var fname = ""
                var fsize = 0L
                list.forEach {
                    val parts = it.split(":")
                    when (parts[0]) {
                        "desc" -> desc = parts[1]
                        "file" -> {
                            fname = parts[1]
                            fsize = parts[2].toLong()
                        }
                    }
                }
                val uploadResponse = UploadResponse(desc, fname, fsize)
                uploadResponse
            }
    }
}
