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
//	fun uploadFileWithoutEntity(@RequestPart("files", required = false) filePartFlux: Flux<PartEvent>): Mono<*> {
//
//		return filePartFlux.flatMap { filePart ->
//			val dataBuffer: DataBuffer = filePart.content()
//			val bytes = ByteArray(dataBuffer.readableByteCount())
//		    dataBuffer.read(bytes) // Read the content into the byte array
//		    println("BUFFER=" + String(bytes))
//
//			val last = filePart.isLast
//			println("Received part event: $filePart, islast = $last")
//			Mono.just(last)
////		    filePart.transferTo(Paths.get(filePart.filename()))
////		        .then(Mono.just(filePart.filename()))
//		}
//			.collectList()
//			.also {
//				println("Collected parts: $it")
//			}
//			.onErrorResume { error -> Mono.error(error) }
//
//
//
////		val resultDto = multiPartSupport.handleMultiPartRequest(allPartsEvents) {
////			val file = nextPartAsTempFile("file")
////			val dto = nextPartAsJson<MediaInfoDto>("metadata")
////			log.warn { "Received (deprecated) storeMediaFile request with media type ${dto.type} for segment SID=$sid" }
////			val newMediaInfo = mediaInfoWithMediaFormat(dto.toEntity(), file)
////			val result = segmentService.updateSegmentBySid(sid) { mediaInfo += newMediaInfo }
////			// get saved media or return fake in case of deduplication or auto-deletion - for backwards compatibility
////			result.mediaInfo.firstOrNull { mediaEvaluator.isDuplicateOf(it, newMediaInfo) }
////				?.toMediaInfoDto(currentBaseUri())
////				?: dto.apply { id = -1 }
////		}
////		return ResponseEntity.ok(resultDto)
//	}
//
//
//}