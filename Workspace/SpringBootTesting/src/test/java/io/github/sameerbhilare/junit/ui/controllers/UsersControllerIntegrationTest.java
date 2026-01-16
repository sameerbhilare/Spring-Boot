package io.github.sameerbhilare.junit.ui.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;


// Option 5
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT) // start at random port irrespective of defined server.port. Internally sets server.port=0

// Option 4
//@SpringBootTest
//@TestPropertySource(locations = "/application-test.properties") // load configuration like server.port from properties file

// Option 3
//@TestPropertySource(locations = "/application-test.properties",
//properties = {"server.port=8082"}) // load from properties file and can override with defined properties here.

// Option 2
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, properties = {"server.port=8888"}) // Starting on defined port with configured port

// Option 1
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)  // Default. This is similar to @WebMvcTest
public class UsersControllerIntegrationTest {

    @Value("${server.port}") // reads value of property server.port
    private int serverPort;

    @LocalServerPort    // actual port where application is running. Useful, specially in case of RANDOM_PORT
    private int localServerPort;

    @Test
    void contextLoaded() {
        System.out.println("server.port=" + serverPort);
        System.out.println("localServerPort=" + localServerPort);
    }
}
