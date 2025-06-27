//package com.eleveo.webflux_leak_02
//
//import io.github.oshai.kotlinlogging.KotlinLogging
//import org.springframework.core.io.buffer.DataBuffer
//import org.springframework.core.io.buffer.DataBufferUtils
//import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
//import org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE
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
//	@PostMapping(
//		"/upload-files20",
//		consumes = [MULTIPART_FORM_DATA_VALUE], produces = [APPLICATION_JSON_VALUE]
//	)
//
////	fun uploadFileWithoutEntity(@RequestPart("files", required = false) filePartFlux: Flux<PartEvent>): Mono<*> {
//	fun uploadFileWithoutEntity(@RequestPart("files", required = false) filePartFlux: Flux<PartEvent>): Flux<*> {
//
////		val parts = request.bodyToFlux<PartEvent>()
//		val windowUntil: Flux<String> = filePartFlux.windowUntil(PartEvent::isLast)
//			.concatMap {
//				it.switchOnFirst { signal, partEvents ->
//					if (signal.hasValue()) {
//						val event = signal.get()
//						if (event is FormPartEvent) {
//							val value: String = event.value();
//							// handle form field
//						} else if (event is FilePartEvent) {
//							val filename: String = event.filename();
//							println("Received file part: $filename")
//							val contents: Flux<DataBuffer> = partEvents.map(PartEvent::content);
//							// handle file upload
//							Mono.just("OK")
//						} else {
////		                    return Mono.error(RuntimeException("Unexpected event: " + event));
//							Mono.just("ERROR")
//						}
//					} else {
//						partEvents; // either complete or error signal
//						Mono.just("PartEvents")
//					}
//
//					partEvents
//					Mono.just("PartEvents2")
//
//				}
//			}
//
//		return windowUntil
//
////		return Mono.just("OK") // Placeholder for actual processing logic
//	}
//
//
//}