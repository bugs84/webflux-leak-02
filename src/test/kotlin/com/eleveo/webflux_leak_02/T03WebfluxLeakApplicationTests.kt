package com.eleveo.webflux_leak_02

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.ClassPathResource
import org.springframework.http.MediaType
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class T03WebfluxLeakApplicationTests {

    @Test
    fun callMultipart() {
        val client = WebClient.create("http://localhost:8080")
        val response = client.post()
            .uri("/upload-files03")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(
                BodyInserters.fromMultipartData(
                    MultipartBodyBuilder().apply {
                        part("files", ClassPathResource("5MG.mp3")) //TODO 5MB file is not in git repo
                        part("files", ClassPathResource("5MG.mp3"))
                    }.build()
                )
            )
            .exchange() //TODO deprecated
            .block()

        val responseBody = response?.bodyToMono<String>()?.block()
        println("Response: '${response?.statusCode()}")
        println("Response Body: '$responseBody'")

        System.gc()
        System.gc()
        System.gc()
        Thread.sleep(2000)
    }

}
