package io.github.sameerbhilare.junit.ui.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers // It is a JUnit Jupiter extension to activate automatic startup and stop of containers used in a test case.
public class UsersControllerWithTestcontainersTest {

    /**
     * T is used to mark the containers that should be managed by the Testcontainers extension.
     * For static containers, the TestContainer will start the container automatically before executing the first test
     * and will stop the container after executing last test case.
     */
    @Container
    private static MySQLContainer mySQLContainer = new MySQLContainer("mysql:8.4.0")
            .withDatabaseName("photo_app")
            .withUsername("sameer")
            .withPassword("sameer");

    // to override properties dynamically
    @DynamicPropertySource
    private static void overrideProperties(DynamicPropertyRegistry registry) {
        // this is required because to avoid conflicts, mysql container will start at random port
        registry.add("spring.datasource.url", mySQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mySQLContainer::getUsername);
        registry.add("spring.datasource.password", mySQLContainer::getPassword);
    }

    @Test
    @DisplayName("The MySQL container is created and is running")
    void testContainerIsRunning() {
        assertTrue(mySQLContainer.isCreated(), "MySQL container has not been created");
        assertTrue(mySQLContainer.isRunning(), "MySQL container is not running");
    }

}
