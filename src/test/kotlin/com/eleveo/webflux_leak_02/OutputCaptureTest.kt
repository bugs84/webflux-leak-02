package com.eleveo.webflux_leak_02

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.io.PrintStream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class OutputCaptureTest {
    private val standardOut = System.out
    private val outputStreamCaptor = ByteArrayOutputStream()

    @BeforeAll
    fun setUp() {
        System.setOut(PrintStream(TeeOutputStream(outputStreamCaptor, standardOut)))
    }

    @AfterAll
    fun tearDownAndVerify() {
        // Restore the standard output
        System.setOut(standardOut)

        // Assert the captured output do not contain 'LEAK: '
        val output = outputStreamCaptor.toString()
        assertThat(output)
            .withFailMessage("Error - Output contain 'LEAK: '")
            .doesNotContain("LEAK: ")
    }

}

private class TeeOutputStream(private val first: OutputStream, private val second: OutputStream) : OutputStream() {
    override fun write(b: Int) {
        first.write(b)
        second.write(b)
    }

    override fun write(b: ByteArray) {
        first.write(b)
        second.write(b)
    }

    override fun write(b: ByteArray, off: Int, len: Int) {
        first.write(b, off, len)
        second.write(b, off, len)
    }

    override fun flush() {
        first.flush()
        second.flush()
    }

    override fun close() {
        first.close()
        second.close()
    }

}