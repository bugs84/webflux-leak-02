//package com.eleveo.webflux_leak_02
//
//import io.github.oshai.kotlinlogging.KotlinLogging
//import org.springframework.core.io.buffer.DataBuffer
//import org.springframework.core.io.buffer.DataBufferUtils
//import org.springframework.http.codec.multipart.FilePartEvent
//import org.springframework.http.codec.multipart.FormPartEvent
//import org.springframework.http.codec.multipart.PartEvent
//import org.springframework.web.bind.annotation.PostMapping
//import org.springframework.web.bind.annotation.RequestPart
//import org.springframework.web.bind.annotation.RestController
//import reactor.core.publisher.Flux
//import reactor.core.publisher.Mono
//import java.nio.file.Paths
//
//private val log = KotlinLogging.logger {}
//
//@RestController
//class MultipartController20Controller {
//
//	var fileNumber = 1
//
//	@PostMapping("/upload-files20")
//	fun uploadFileWithoutEntity(@RequestPart("files", required = false) filePartFlux: Flux<PartEvent>): Mono<*> {
////		val blockFirst = filePartFlux.map { it ->
////
////			val last = it.isLast
////			println("Received part event: $it, isLast: $last")
////		}
//
//
//
//		return filePartFlux.windowUntil(PartEvent::isLast)
//			.concatMap { partEvents ->
//				partEvents.switchOnFirst { signal, events ->
//					if (signal.hasValue()) {
//						val event = signal.get()
//						when (event) {
//							is FormPartEvent -> {
//								val value = event.value()
//								throw IllegalStateException("Form part event not expected here: $value")
//								// Handle form field
//								Mono.just("Form field processed: $value")
//							}
//
//							is FilePartEvent -> {
//								val filename = event.filename()
//								val contents: Flux<DataBuffer> = events.map(PartEvent::content)
//								// Handle file upload
//								val fileDestination = Paths.get("generated_${fileNumber++}.file")
//								println("Going to write into file: '$fileDestination'")
//								DataBufferUtils.write(contents, fileDestination)
//									.then(Mono.just("File uploaded: $filename"))
//							}
//
//							else -> Mono.error(RuntimeException("Unexpected event: $event"))
//						}
//					} else {
//						events // Either complete or error signal
//					}
//				}
//			}
//			.then(Mono.just("OK"))
//			.onErrorResume { error: Throwable ->
//				log.error(error) { "Error uploading files" }
//				throw error
//			}
//	}
//
//
//}