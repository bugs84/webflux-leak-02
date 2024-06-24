package com.eleveo.webflux_leak_02

import org.apache.hc.client5.http.entity.mime.InputStreamBody
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder
import org.apache.hc.client5.http.impl.classic.HttpClients
import org.apache.hc.core5.http.ContentType
import org.apache.hc.core5.http.io.entity.EntityUtils
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class T10WebfluxLeakApplicationTests : OutputCaptureTest() {

    @Test
    fun testMultipleTimes() {
        callMultipart()
        callMultipart()
    }

    //    @Test
    fun callMultipart() {
        val httpClient = HttpClients.createDefault()

        val req = ClassicRequestBuilder.post("http://localhost:8080/upload-files10").build()

        val reqEntity = MultipartEntityBuilder.create()
            .addPart("files", InputStreamBody(RandomByteInputStream(5 * 1024 * 1024), ContentType.parse("audio/mpeg")))
            .addPart("files", InputStreamBody(RandomByteInputStream(5 * 1024 * 1024), ContentType.parse("audio/mpeg")))
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
        Thread.sleep(1000)
    }

}
