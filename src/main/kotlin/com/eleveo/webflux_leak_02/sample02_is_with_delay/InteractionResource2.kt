package com.eleveo.webflux_leak_02.sample02_is_with_delay

import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.multipart.PartEvent
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

@RestController
class InteractionResource2(
	private val multiPartSupport: MultiPartSupport2
) {

	@PostMapping(
		"/sample02",
		consumes = [MULTIPART_FORM_DATA_VALUE], produces = [APPLICATION_JSON_VALUE]
	)
	suspend fun importInteractionWithMedia(
		@RequestBody allPartsEvents: Flux<PartEvent>
	): ResponseEntity<InteractionDto> {
		val result: List<SegmentUnitOfWork> = handleInteractionImportRequestWithFiles(allPartsEvents)
		val interactionDto = getOrFakeInteraction(result)
		return ResponseEntity.ok(interactionDto)
	}

	private suspend fun handleInteractionImportRequestWithFiles(allPartsEvents: Flux<PartEvent>): List<SegmentUnitOfWork> =
		multiPartSupport.handleMultiPartRequest(allPartsEvents) {
			val result = mutableListOf<SegmentUnitOfWork>()

			val file1 = nextPartAsTempFile("files")
			result.add(SegmentUnitOfWork(description = "File1: ${file1.path.fileName}"))

			val file2 = nextPartAsTempFile("files")
			result.add(SegmentUnitOfWork(description = "File2: ${file2.path.fileName}"))


			result
		}


	private suspend fun getOrFakeInteraction(result: List<SegmentUnitOfWork>): InteractionDto {
		return InteractionDto("INTERACTION { unitOfWorks=[${result.map { it.description }.joinToString { "," }}]}")
	}

	data class SegmentUnitOfWork(
		val description: String,
	)

	data class InteractionDto(
		val description: String,
	)

}



