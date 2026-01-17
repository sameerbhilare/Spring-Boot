package io.github.sameerbhilare.junit.ui.controllers;

import io.github.sameerbhilare.junit.security.SecurityConstants;
import io.github.sameerbhilare.junit.ui.response.UserRest;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import java.util.Arrays;
import java.util.List;


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

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UsersControllerIntegrationTest {

    @Value("${server.port}") // reads value of property server.port
    private int serverPort;

    @LocalServerPort    // actual port where application is running. Useful, specially in case of RANDOM_PORT
    private int localServerPort;

    @Autowired
    // If you are using the @SpringBootTest annotation with an embedded server,
    // a TestRestTemplate and/or WebTestClient is automatically available and can be @Autowired into your test.
    // we may inject RestTemplate, but it is safe to use TestRestTemplate in integration tests.
    private TestRestTemplate testRestTemplate;

    private String authorizationToken;

    @Test
    void contextLoaded() {
        System.out.println("server.port=" + serverPort);
        System.out.println("localServerPort=" + localServerPort);
    }

    @Test
    @DisplayName("User can be created")
    @Order(1)
    void testCreateUser_whenValidDetailsProvided_returnsUserDetails() throws JSONException {
        // Arrange
//        String createUserJson = "{\n" +
//                "    \"firstName\":\"Sameer\",\n" +
//                "    \"lastName\":\"Bhilare\",\n" +
//                "    \"email\":\"test3@test.com\",\n" +
//                "    \"password\":\"12345678\",\n" +
//                "    \"repeatPassword\":\"12345678\"\n" +
//                "}";

        JSONObject userDetailsRequestJson = new JSONObject();
        userDetailsRequestJson.put("firstName", "Sameer");
        userDetailsRequestJson.put("lastName", "Bhilare");
        userDetailsRequestJson.put("email", "test@test.com");
        userDetailsRequestJson.put("password","12345678");
        userDetailsRequestJson.put("repeatPassword", "12345678");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

        HttpEntity<String> request = new HttpEntity<>(userDetailsRequestJson.toString(), headers);

        // Act
        ResponseEntity<UserRest> createdUserDetailsEntity = testRestTemplate.postForEntity("/users",
                request,
                UserRest.class);
        // DB - HikariDataSource automatically configures H2 database. See test server logs and search for h2
        UserRest createdUserDetails = createdUserDetailsEntity.getBody();

        // Assert
        Assertions.assertEquals(HttpStatus.OK, createdUserDetailsEntity.getStatusCode());
        Assertions.assertEquals(userDetailsRequestJson.getString("firstName"),
                createdUserDetails.getFirstName(),
                "Returned user's first name seems to be incorrect");
        Assertions.assertEquals(userDetailsRequestJson.getString("lastName"),
                createdUserDetails.getLastName(),
                "Returned user's last name seems to be incorrect");
        Assertions.assertEquals(userDetailsRequestJson.getString("email"),
                createdUserDetails.getEmail(),
                "Returned user's email seems to be incorrect");
        Assertions.assertFalse(createdUserDetails.getUserId().trim().isEmpty(),
                "User id should not be empty");
    }

    @Test
    @DisplayName("GET /users requires JWT")
    @Order(2)
    void testGetUsers_whenMissingJWT_returns403() {
        // Arrange
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        HttpEntity requestEntity = new HttpEntity(null, headers);

        // Act
        ResponseEntity<List<UserRest>> response = testRestTemplate.exchange("/users",
                HttpMethod.GET,
                requestEntity,
                new ParameterizedTypeReference<List<UserRest>>() {
                });

        // Assert
        Assertions.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode(),
                "HTTP Status code 403 Forbidden should have been returned");
    }

    @Test
    @DisplayName("/login works")
    @Order(3)
    void testUserLogin_whenValidCredentialsProvided_returnsJWTinAuthorizationHeader() throws JSONException {
        // Arrange
//        String loginCredentialsJson = "{\n" +
//                "    \"email\":\"test3@test.com\",\n" +
//                "    \"password\":\"12345678\"\n" +
//                "}";
        JSONObject loginCredentials = new JSONObject();
        loginCredentials.put("email","test@test.com");
        loginCredentials.put("password","12345678");

        HttpEntity<String> request = new HttpEntity<>(loginCredentials.toString());

        // Act
        ResponseEntity response = testRestTemplate.postForEntity("/users/login",
                request,
                null);

        authorizationToken = response.getHeaders().
                getValuesAsList(SecurityConstants.HEADER_STRING).get(0);

        // Assert
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(),
                "HTTP Status code should be 200");
        Assertions.assertNotNull(authorizationToken,
                "Response should contain Authorization header with JWT");
        Assertions.assertNotNull(response.getHeaders().
                        getValuesAsList("UserID").get(0),
                "Response should contain UserID in a response header");
    }
}
