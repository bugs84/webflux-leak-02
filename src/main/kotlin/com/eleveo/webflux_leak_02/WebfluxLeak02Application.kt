package com.eleveo.webflux_leak_02

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.http.codec.multipart.PartEventHttpMessageReader
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.reactive.config.WebFluxConfigurer

@SpringBootApplication
class WebfluxLeak02Application

fun main(args: Array<String>) {
	runApplication<WebfluxLeak02Application>(*args)
}
