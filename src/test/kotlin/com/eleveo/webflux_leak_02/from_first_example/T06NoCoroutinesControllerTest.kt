package com.eleveo.webflux_leak_02.from_first_example

import org.apache.hc.client5.http.entity.mime.InputStreamBody
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder
import org.apache.hc.client5.http.entity.mime.StringBody
import org.apache.hc.client5.http.impl.classic.HttpClients
import org.apache.hc.core5.http.ContentType
import org.apache.hc.core5.http.io.entity.EntityUtils
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.ClassPathResource
import org.springframework.http.MediaType
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class T06NoCoroutinesControllerTest {

    @Test
    fun testMultipleTimes() {
        callMultipart()
        callMultipart()
        callMultipart()
        callMultipart()
        callMultipart()
    }

    @Test
    fun callMultipart() {  // TODO This Caused LEAK - but after many runs of different tests  :o
        val httpClient = HttpClients.createDefault()

        val req = ClassicRequestBuilder.post("http://localhost:8080/interactions/import06").build()

        val reqEntity = MultipartEntityBuilder.create()
//            .addPart("interaction", StringBody("""{"dummyValue": "This is some text"}""", ContentType.APPLICATION_JSON))
            .addPart("media", InputStreamBody(ClassPathResource("5MG.mp3").inputStream, ContentType.parse("audio/mpeg")))
            .addPart("media", InputStreamBody(ClassPathResource("5MG.mp3").inputStream, ContentType.parse("audio/mpeg")))
//            .addPart("media", InputStreamBody(ClassPathResource("5MG.mp3").inputStream, ContentType.parse("audio/mpeg")))
            .build()
        req.entity = reqEntity


        httpClient.execute(req) { response ->
            println("""RESPONSE: ${response.code} ${response.reasonPhrase}""");
            assertThat(response.code).isEqualTo(200)
            val entity1 = response.entity;
            println(EntityUtils.toString(entity1));
            EntityUtils.consume(entity1);
            null;
        }

        httpClient.close()

        System.gc()
        System.gc()
        System.gc()
        Thread.sleep(2000)
    }

//    @Test
//    fun callMultipart2() {
//        val client = WebClient.create("http://localhost:8080")
//        val response = client.post()
//            .uri("/multipart")
//            .contentType(MediaType.MULTIPART_FORM_DATA)
//            .body(
//                BodyInserters.fromMultipartData(
//                    MultipartBodyBuilder().apply {
//                        part("introduction", "test")
//                        part("file", ClassPathResource("5MG.mp3"))
//                        part("file", ClassPathResource("5MG.mp3"))
//                    }.build()
//                )
//            )
//            .exchange()
//            .block()
//
//        val responseBody = response?.bodyToMono<String>()?.block()
//        println("Response: '${response?.statusCode()}")
//        println("Response Body: '$responseBody'")
//    }

}
