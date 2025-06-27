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
import org.springframework.boot.test.context.SpringBootTest
import kotlin.random.Random

private val log = KotlinLogging.logger {}

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class T20WebfluxLeakApplicationTests : OutputCaptureTest() {

    @Test
    fun testMultipleTimes() {
        repeat(100) {
            println("Iteration: $it")
            callMultipart()
        }

        System.gc()
        System.gc()
        System.gc()
        Thread.sleep(1000)
    }

    fun callMultipart() {
        val httpClient = HttpClients.createDefault()

//        val req = ClassicRequestBuilder.post("http://localhost:8080/load").build()
        val req = ClassicRequestBuilder.post("http://localhost:8080/upload-simple2").build()

        val reqEntity = MultipartEntityBuilder.create()
            .addPart(
                "files",
                InputStreamBody(RandomByteInputStream(10172), ContentType.parse("audio/mpeg"))
            )
//			.addPart(
//				"files",
//				InputStreamBody(RandomByteInputStream(10172), ContentType.parse("audio/mpeg"))
//			)
            .addPart(
                "files",
                InputStreamBody(
//					FailingRandomByteInputStream(size = 6824, failWhenRemaining = Random.nextInt(10).also { log.info { "Failing at $it bytes"} }),
                    FailingRandomByteInputStream(size = 6824, failWhenRemaining = 1200),
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

    }

}

