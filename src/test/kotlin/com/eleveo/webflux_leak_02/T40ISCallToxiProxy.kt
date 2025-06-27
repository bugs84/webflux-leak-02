package com.eleveo.webflux_leak_02

import eu.rekawek.toxiproxy.Proxy
import eu.rekawek.toxiproxy.ToxiproxyClient
import eu.rekawek.toxiproxy.model.ToxicDirection.*
import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.hc.client5.http.entity.mime.InputStreamBody
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder
import org.apache.hc.client5.http.impl.classic.HttpClients
import org.apache.hc.core5.http.ContentType
import org.apache.hc.core5.http.NoHttpResponseException
import org.apache.hc.core5.http.io.entity.EntityUtils
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest
import org.testcontainers.Testcontainers
import org.testcontainers.containers.ToxiproxyContainer
import org.testcontainers.containers.output.Slf4jLogConsumer


private val log = KotlinLogging.logger {}

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class T40ISCallToxiProxy : OutputCaptureTest() {

    var proxyPort: Int = -1
    lateinit var appProxy: Proxy

    @BeforeAll
    fun setup() {
        log.info { "Starting ToxyProxy test..." }

        Testcontainers.exposeHostPorts(8080)
        val toxyproxy = ToxiproxyContainer("ghcr.io/shopify/toxiproxy:2.11.0")
            .withLogConsumer(Slf4jLogConsumer(LoggerFactory.getLogger("testcontainers.proxy")).withSeparateOutputStreams())
            .withExposedPorts(8474, 8080)
        toxyproxy.start(

        )

        val toxiproxyClient = ToxiproxyClient(toxyproxy.getHost(), toxyproxy.getControlPort());
        appProxy = toxiproxyClient.createProxy("app-proxy", "0.0.0.0:8080", "host.testcontainers.internal:8080")

        proxyPort = toxyproxy.getMappedPort(8080)
        log.info { "App ToxiProxy started on port: $proxyPort" }

    }


    @Test
    fun testMultipleTimes() {
        appProxy.toxics().limitData("limitData", UPSTREAM, 15_000)
        repeat(10) {
            println("Iteration: $it")
            callMultipart()
        }
    }

    fun callMultipart() {

        val httpClient = HttpClients.createDefault()

        val req = ClassicRequestBuilder.post("http://localhost:$proxyPort/load").build()

        val reqEntity = MultipartEntityBuilder.create()
            .addPart(
                "files",
                InputStreamBody(RandomByteInputStream(10172), ContentType.parse("audio/mpeg"))
            )
            .addPart(
                "files",
                InputStreamBody(RandomByteInputStream(size = 6824), ContentType.parse("audio/mpeg"))
            )
            .build()
        req.entity = reqEntity


        try {
            httpClient.execute(req) { response ->
                log.info { """RESPONSE: ${response.code} ${response.reasonPhrase}""" }
                assertThat(response.code).isEqualTo(200)
                val entity1 = response.entity
                log.info { "Body: '${EntityUtils.toString(entity1)}'" }
                EntityUtils.consume(entity1)
                null;
            }
            throw IllegalStateException("Expected to fail due to ToxyProxy limitData toxic, but it didn't fail.")
        } catch (e: NoHttpResponseException) {
            log.info { "Broken due to toxyProxy" }
        } finally {
            httpClient.close()
        }

        System.gc()
        System.gc()
        System.gc()
        Thread.sleep(1000)
    }

}

