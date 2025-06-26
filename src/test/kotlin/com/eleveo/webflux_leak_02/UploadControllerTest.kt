package com.eleveo.webflux_leak_02

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.ClassPathResource
import org.springframework.http.MediaType
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UploadControllerTest {

    @Autowired
    lateinit var client: WebTestClient

    @Test
    fun `test file upload`() {
        val resource = ClassPathResource("test.txt")
        val builder = MultipartBodyBuilder()
        builder.part("description", "Test file upload")
        builder.part("file", resource)

        client.post()
            .uri("/upload")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .bodyValue(builder.build())
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.description").isEqualTo("Test file upload")
            .jsonPath("$.fileName").isEqualTo("test.txt")
            .jsonPath("$.fileSize").isEqualTo(resource.contentLength())
    }
}
