package com.eleveo.webflux_leak_02.sample02_is_with_delay

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consume
import kotlinx.coroutines.channels.getOrElse
import kotlinx.coroutines.channels.onSuccess
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.produceIn
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactive.collect
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.reactor.mono
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.HttpHeaders
import org.springframework.http.codec.multipart.PartEvent
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import java.io.SequenceInputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption.TRUNCATE_EXISTING

private val extensionSanitizeRegex = "\\W+".toRegex()

private val log = KotlinLogging.logger {}

@Component
class MultiPartSupport2(
//	private val mediaService: MediaService,
//	private val observationRegistry: ObservationRegistry,
	private val objectMapper: ObjectMapper,
//	private val properties: InteractionServiceProperties,
) {
//	private val tmpLocation get() = properties.mediaLocationPath.resolve("tmp")
	private val tmpLocation get() = Path.of("tmp")
//	private val ioContext get() = Dispatchers.IO + observationRegistry.asContextElement()
	private val ioContext get() = Dispatchers.IO

	suspend fun <R> handleMultiPartRequest(
		allPartsEvents: Flux<PartEvent>,
		handleFn: suspend MultiPartRequest.() -> R
	): R = coroutineScope {
		allPartsEvents.windowUntil(PartEvent::isLast).asFlow().produceIn(this).consume {
			try {
//				mediaService.inTransaction { MultiPartRequest(this).handleFn() }
				mediaServiceInTransaction { MultiPartRequest(this).handleFn() }
			} finally {
				// ensure all buffers are always fully released - especially in case of a failure
				consumeAsFlow().collect { remainingPart ->
					remainingPart.map { it.content() }.subscribe(DataBufferUtils.releaseConsumer()) // release untouched content
				}
			}
		}
	}

		private suspend inline fun <R> mediaServiceInTransaction(crossinline function: suspend () -> R): R {
			return function()
		}

	/**
	 * Object that exposes additional functions for handling multipart messages,
	 * such as parsing JSONs and saving files to temp
	 * with forced cleanup of all temp files when this object is closed.
	 */
	inner class MultiPartRequest(private val parts: ReceiveChannel<Flux<PartEvent>>) {

		private suspend fun getNextPart(name: String) =
			parts.receiveCatching()
				.getOrElse { throw IllegalArgumentException("Not enough parts. Expected part with \"$name\".") }

		suspend inline fun <reified T> nextPartAsJson(partName: String) = nextPartAsJson(partName, T::class.java)
		suspend fun <T> nextPartAsJson(partName: String, dtoClass: Class<T>) =
			getNextPart(partName).parseJson(partName, dtoClass)

		suspend fun nextPartAsTempFile(partName: String) =
			getNextPart(partName).asTempFilePart(partName, /*currentMediaTransaction()*/)

//		suspend fun nextPartAsMedia(partName: String, segment: Segment, mediaInfo: MediaInfo) =
//			getNextPart(partName).asMediaContentFor(partName, segment, mediaInfo, currentMediaTransaction())

		suspend fun checkNoMoreParts() = parts.receiveCatching().onSuccess { remainingPart ->
			remainingPart.map { it.content() }.subscribe(DataBufferUtils.releaseConsumer()) // release untouched content
			throw IllegalArgumentException("Unexpected extra parts in multipart request")
		}

		private fun checkPartName(event: PartEvent, partName: String) {
			log.trace { "Processing part \"${event.name()}\" with headers: ${event.headers()}" }
			require(event.name() == partName) {
				"Invalid part. Expected part \"$partName\", got \"${event.name()}\" with headers: ${event.headers()}"
			}
		}

		private suspend fun <T> Flux<PartEvent>.parseJson(partName: String, dtoClass: Class<T>): T {
			val buffers = mutableListOf<PartEvent>()
			try {
				collect { buffers += it }
				checkPartName(buffers.first(), partName)
				val inputStream = buffers.map { it.content().asInputStream() }.reduce(::SequenceInputStream)
				return objectMapper.readerFor(dtoClass).readValue(inputStream)
			} finally {
				buffers.forEach {
					log.trace { "Releasing JSON part buffer: ${it.content()}" }
					DataBufferUtils.release(it.content())
				}
			}
		}

//		private suspend fun Flux<PartEvent>.asMediaContentFor(
//			partName: String, segment: Segment, mediaInfo: MediaInfo, mediaTransaction: MediaTransaction
//		): MediaInfo =
//			switchOnFirst { signal, events ->
//				val event = signal.get() ?: return@switchOnFirst events.cast(MediaInfo::class.java) // propagate error/cancel
//				mono(observationRegistry.asContextElement()) {
//					val content = events.map(PartEvent::content)
//					val mediaWithFormat = try {
//						checkPartName(event, partName)
//						mediaInfoWithMediaFormat(mediaInfo, event.headers())
//					} catch (exc: Exception) {
//						content.subscribe(DataBufferUtils.releaseConsumer()) // release untouched content on failure
//						throw exc
//					}
//					// since we *MUST* fully read the part content, we also immediately await() the result here too
//					mediaTransaction.prepareToSaveMediaFile(segment, mediaWithFormat, content).await()
//				}
//			}.awaitSingle()

		private suspend fun Flux<PartEvent>.asTempFilePart(
			partName: String,
//			mediaTransaction: MediaTransaction
		): TempFilePart =
			switchOnFirst { signal, events ->
				val event = signal.get() ?: return@switchOnFirst events.cast(TempFilePart::class.java) // propagate error/cancel
				mono(ioContext) {
					delay(10000) //IMPORTANT DELAY
					val filename = event.headers().contentDisposition.filename ?: ""
					val content = events.map(PartEvent::content)
					val tempFile = try {
						checkPartName(event, partName)
						val extension = filename.getExtensionSanitized() ?: "tmp"
						Files.createTempFile(tmpLocation, "multipart-", ".$extension") // blocking operation
					} catch (exc: Exception) {
						content.subscribe(DataBufferUtils.releaseConsumer()) // release on failure
						throw exc
					}
					// mediaTransaction.onCleanup { tempFile.deleteIfExists() } - Removed No cleanup after transaction
					DataBufferUtils.write(content, tempFile, TRUNCATE_EXISTING).awaitSingleOrNull()
					TempFilePart(event.headers(), tempFile)
				}
			}.awaitSingle()



//		fun mediaInfoWithMediaFormat(mediaInfo: MediaInfo, headers: HttpHeaders) =
//			if (mediaInfo.mediaFormat != null) {
//				mediaInfo
//			} else {
//				val mediaFormat = SupportedMediaFormat.from(
//					mediaType = headers.contentType,
//					mediaInfoType = mediaInfo.type,
//					filename = headers.contentDisposition.filename,
//				)
//				mediaInfo.copy(mediaFormat = mediaFormat, type = mediaFormat.mediaInfoType)
//			}

//		fun mediaInfoWithMediaFormat(mediaInfo: MediaInfo, tempFile: TempFilePart): MediaInfo {
//			val mediaFormat = mediaInfo.mediaFormat ?: SupportedMediaFormat.from(
//				mediaType = tempFile.headers.contentType,
//				mediaInfoType = mediaInfo.type,
//				filename = tempFile.headers.contentDisposition.filename,
//			)
//			return mediaInfo.copy(
//				location = MediaInfoUriConverter.pathToUri(tempFile.path.pathString),
//				mediaFormat = mediaFormat,
//				type = mediaFormat.mediaInfoType,
//			)
//		}

	}

	/**
	 * One part of multipart request stored as a file in `${media-location}/tmp`.
	 */
	data class TempFilePart(val headers: HttpHeaders, val path: Path)
}

private fun String.getExtensionSanitized(): String? =
	substringAfterLast(".", "").replace(extensionSanitizeRegex, "").lowercase().takeUnless { it.isBlank() }
