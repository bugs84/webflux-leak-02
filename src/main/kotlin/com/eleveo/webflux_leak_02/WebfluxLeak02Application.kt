package com.eleveo.webflux_leak_02

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class WebfluxLeak02Application

fun main(args: Array<String>) {
	runApplication<WebfluxLeak02Application>(*args)
}
