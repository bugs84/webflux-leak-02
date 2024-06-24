package com.eleveo.webflux_leak_02

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono


@RestController
class HelloWorldController {
    @GetMapping("/hello")
    fun hello(): Mono<String> {
        return Mono.just("Hello, World!")
    }
} 