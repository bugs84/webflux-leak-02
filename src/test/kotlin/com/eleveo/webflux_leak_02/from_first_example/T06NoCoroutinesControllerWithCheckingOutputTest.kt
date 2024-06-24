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
import java.io.InputStream
import java.io.SequenceInputStream
import java.util.Collections
import kotlin.random.Random

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class T06NoCoroutinesControllerWithCheckingOutputTest {

    @Test
    fun testMultipleTimes() {
        callMultipart()
        callMultipart()
    }

//    @Test
    fun callMultipart() {  
        val httpClient = HttpClients.createDefault()

        val req = ClassicRequestBuilder.post("http://localhost:8080/interactions/import06").build()

        val reqEntity = MultipartEntityBuilder.create()
//            .addPart("interaction", StringBody("""{"dummyValue": "This is some text"}""", ContentType.APPLICATION_JSON))
            .addPart("media", InputStreamBody(generateRandomByteStream(5*1024*1024), ContentType.parse("audio/mpeg")))
            .addPart("media", InputStreamBody(generateRandomByteStream(5*1024*1024), ContentType.parse("audio/mpeg")))
//            .addPart("media", InputStreamBody(ClassPathResource("5MG.mp3").inputStream, ContentType.parse("audio/mpeg")))
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

    fun generateRandomByteStream(sizeInBytes: Int): InputStream {
        val chunkSize = 1024
        val chunkCount = sizeInBytes / chunkSize

        val chunkInputStreams = Collections.nCopies(chunkCount, RandomByteInputStream(chunkSize))
        return SequenceInputStream(Collections.enumeration(chunkInputStreams))
    }

    class RandomByteInputStream(private val size: Int) : InputStream() {
        private var remaining: Int = size

        override fun read(): Int {
            return if (remaining > 0) {
                remaining--
                Random.nextInt(256)
            } else {
                -1
            }
        }
    }

}
