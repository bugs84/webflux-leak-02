package com.eleveo.webflux_leak_02

import java.io.InputStream
import kotlin.random.Random

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