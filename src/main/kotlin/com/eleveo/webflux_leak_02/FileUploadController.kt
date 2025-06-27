//package com.eleveo.webflux_leak_02
//
//import org.springframework.http.codec.multipart.PartEvent
//import org.springframework.web.bind.annotation.*
//import reactor.core.publisher.Flux
//import reactor.core.publisher.Mono
//import org.springframework.http.MediaType
//import org.springframework.web.bind.annotation.RestController
//import reactor.kotlin.core.publisher.toFlux
//import java.nio.charset.StandardCharsets
//
//@RestController
//class FileUploadController {
//
//    @PostMapping(
//        "/upload-files20",
//        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE],
//        produces = [MediaType.APPLICATION_JSON_VALUE]
//    )
//    fun uploadFileWithoutEntity(
//        @RequestPart("files", required = false) filePartFlux: Flux<PartEvent>
//    ): Mono<Map<String, Any>> {
//
//        return filePartFlux
//            .flatMap { partEvent ->
//                when (partEvent) {
//                    is PartEvent.PartHeaders -> {
//                        println("Start part: ${partEvent.name}")
//                        Mono.empty()
//                    }
//                    is PartEvent.PartData -> {
//                        val byteBuffer = partEvent.content
//                        val contentStr = StandardCharsets.UTF_8.decode(byteBuffer.asByteBuffer()).toString()
//                        println("Received data chunk: $contentStr")
//                        Mono.empty()
//                    }
//                    is PartEvent.PartEnd -> {
//                        println("End part")
//                        Mono.empty()
//                    }
//                    else -> Mono.empty()
//                }
//            }
//            .thenReturn(mapOf("status" to "Upload processed"))
//    }
//}