package com.eleveo.webflux_leak_02

import java.io.InputStream
import kotlin.random.Random

class FailingRandomByteInputStream(private val size: Int, val failWhenRemaining: Int) : InputStream() {
    private var remaining: Int = size

    override fun read(): Int {
        return if(remaining <= failWhenRemaining) {
            //Thread.sleep(1000*60*60*24) //Simulate stuck
            throw MiddleStreamException() //Throw exception somewhere inside generating of the stream
        } else if (remaining > 0) {
            remaining--
            Random.nextInt(256)
        } else {
            -1
        }
    }
}

class MiddleStreamException: Exception()