package com.eleveo.webflux_leak_02

import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.hc.client5.http.entity.mime.InputStreamBody
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder
import org.apache.hc.client5.http.impl.classic.HttpClients
import org.apache.hc.core5.http.ContentType
import org.apache.hc.core5.http.io.entity.EntityUtils
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder
import org.apache.hc.core5.io.CloseMode
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.ClassPathResource
import org.springframework.http.MediaType
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.test.web.reactive.server.WebTestClient

private val log = KotlinLogging.logger {}

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class UploadControllerTest : OutputCaptureTest() {

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


    @Test
   	fun testMultipleTimes() {
   		repeat(2) {
   			println("Iteration: $it")
   			callMultipart()
   		}
   	}

   	fun callMultipart() {
   		val httpClient = HttpClients.createDefault()

   		val req = ClassicRequestBuilder.post("http://localhost:8080/upload").build()

   		val reqEntity = MultipartEntityBuilder.create()
   			.addPart(
   				"files",
   				InputStreamBody(RandomByteInputStream(10172), ContentType.parse("audio/mpeg"))
   			)
   			.addPart(
   				"files",
   				InputStreamBody(
   					FailingRandomByteInputStream(size = 6824, failWhenRemaining = 2000),
   					ContentType.parse("audio/mpeg")
   				)
   			)
   			.build()
   		req.entity = reqEntity


   		var clientClosed = false
   		try {
   			httpClient.execute(req) { response ->
   				log.info { """RESPONSE: ${response.code} ${response.reasonPhrase}""" }
   				assertThat(response.code).isEqualTo(200)
   				val entity1 = response.entity
   				log.info { "Body: '${EntityUtils.toString(entity1)}'" }
   				EntityUtils.consume(entity1)
   				null;
   			}
   		} catch (e: MiddleStreamException) {
   //			httpClient.close()
   			httpClient.close(CloseMode.IMMEDIATE)
   			clientClosed = true
   		}


   		if (!clientClosed) throw IllegalStateException("Client was not closed.")

   //		finally {
   //
   //		}
   //			httpClient.close()

   		System.gc()
   		System.gc()
   		System.gc()
   		Thread.sleep(1000)
   	}


}
