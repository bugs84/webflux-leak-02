# webflux-leak-02

Simple sample to reproduce a 'LEAK: ' in a simple WebFlux application which is using Multipart.

# How to reproduce the leak

Run test: `kotlin/com/eleveo/webflux_leak_02/T10WebfluxLeakApplicationTests.kt`

# Content

`kotlin/com/eleveo/webflux_leak_02/MultipartController10Controller.kt`
Simple serverside controller, that accepts multipart request.

`kotlin/com/eleveo/webflux_leak_02/T10WebfluxLeakApplicationTests.kt`
Test which simulate the leak.

`kotlin/com/eleveo/webflux_leak_02/T10WebfluxLeakApplicationTests.01.txt`
Sample output of the test, which contains 'LEAK: '.

Netty leak detection is set to `leak-detection: paranoid` in `application.yml`.

