package io.github.sameerbhilare.junit.ui.controllers;

import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers // It is a JUnit Jupiter extension to activate automatic startup and stop of containers used in a test case.
public class UsersControllerWithTestcontainersTest {
}
