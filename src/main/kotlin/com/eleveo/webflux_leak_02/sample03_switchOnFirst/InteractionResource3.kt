package com.eleveo.webflux_leak_02.sample03_switchOnFirst

import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.multipart.PartEvent
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

@RestController
class InteractionResource3(
	private val multiPartSupport: MultiPartSupport3
) {

	@PostMapping(
		"/sample03",
		consumes = [MULTIPART_FORM_DATA_VALUE], produces = [APPLICATION_JSON_VALUE]
	)
	suspend fun importInteractionWithMedia(
		@RequestBody allPartsEvents: Flux<PartEvent>
	): ResponseEntity<String> {
		handleInteractionImportRequestWithFiles(allPartsEvents)
		return ResponseEntity.ok("Static response")
	}

	private suspend fun handleInteractionImportRequestWithFiles(allPartsEvents: Flux<PartEvent>): Unit =
		multiPartSupport.handleMultiPartRequest(allPartsEvents) {
			//This is expected just two parts 2x "files" nothing else
			val file1 = nextPartAsTempFile("files")
			val file2 = nextPartAsTempFile("files")
		}
}



