package com.eleveo.webflux_leak_02.sample06_flux_experiments

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE
import org.springframework.http.codec.multipart.PartEvent
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration

private val log = KotlinLogging.logger {}

@RestController
class UploadControllerSimple2 {

	@PostMapping("/sample06", consumes = [MULTIPART_FORM_DATA_VALUE], produces = [APPLICATION_JSON_VALUE])
	fun upload(@RequestBody allParts: Flux<PartEvent>): Mono<String> {
		return allParts
			.doOnNext {
				log.info { "Event First doOnNext: ${it.content()}" }
			}
			//            .delayElements(Duration.ofSeconds(10))
			//            .flatMap { Mono.defer { Mono.just(it).delaySubscription(Duration.ofSeconds(10)) } }
			//            .flatMap {  Mono.just(it).delaySubscription(Duration.ofSeconds(10))  }
			.flatMap { partEvent ->
				Mono.defer {
					Mono.just(partEvent).delaySubscription(Duration.ofSeconds(10))
						//                        .onErrorResume {
						//                            log.info { "Event beforeRelease: ${partEvent.content()}" }
						//                            DataBufferUtils.release(partEvent.content())
						//                            Mono.error(it)
						//                        }
						.doFinally {
							log.info { "Event beforeRelease: ${partEvent.content()}" }
							DataBufferUtils.release(partEvent.content())
						}
				}
			}
			.doOnNext {
				log.info { "Event buffer size: ${it.content()}" }
				//                DataBufferUtils.release(it.content())
			}

			.then(Mono.just("Response text"))
	}
}


