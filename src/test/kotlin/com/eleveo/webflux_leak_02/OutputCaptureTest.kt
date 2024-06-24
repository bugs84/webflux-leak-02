package com.eleveo.webflux_leak_02

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.assertj.core.api.Assertions.assertThat
import java.io.ByteArrayOutputStream
import java.io.PrintStream

abstract class OutputCaptureTest {
    private val standardOut = System.out
    private val outputStreamCaptor = ByteArrayOutputStream()

    @BeforeEach
    fun setUp() {
        System.setOut(PrintStream(outputStreamCaptor))
    }

    @AfterEach
    fun tearDownAndVerify() {
        // Restore the standard output
        System.setOut(standardOut)

        // Assert the captured output do not contain 'LEAK: '
        val output = outputStreamCaptor.toString()
        assertThat(output)
            .`as`("Error - Output contain 'LEAK: '")
            .doesNotContain("LEAK: ")
    }

}