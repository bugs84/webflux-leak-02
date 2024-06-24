package com.eleveo.webflux_leak_02

import io.github.oshai.kotlinlogging.KotlinLogging
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.InputStreamResource
import org.springframework.http.MediaType
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

private val log = KotlinLogging.logger {}

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class T11WebfluxLeakApplicationTests : OutputCaptureTest() {

    @Test
    fun testMultipleTimes() {
        callMultipart()
        callMultipart()
    }

    //@Test
    fun callMultipart() {

        val client = WebClient.create("http://localhost:8080")
        val response = client.post()
            .uri("/upload-files11")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(
                BodyInserters.fromMultipartData(
                    MultipartBodyBuilder().apply {
                        part(
                            "files",
                            InputStreamResource(RandomByteInputStream(5 * 1024 * 1024))/*, MediaType.parseMediaType("audio/mpeg")*/
                        )
                        part(
                            "files",
                            InputStreamResource(RandomByteInputStream(5 * 1024 * 1024))/*, MediaType.parseMediaType("audio/mpeg")*/
                        )
                    }.build()
                )
            )
            .exchange()
            .block()


        assertThat(response!!.statusCode().value()).isEqualTo(200)
        val responseBody = response.bodyToMono<String>().block()
        log.info { "Response Body: '$responseBody'" }

        System.gc()
        System.gc()
        System.gc()
        Thread.sleep(1000)
    }

}
