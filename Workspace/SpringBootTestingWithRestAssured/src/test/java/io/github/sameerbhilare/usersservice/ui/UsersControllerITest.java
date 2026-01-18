package io.github.sameerbhilare.usersservice.ui;

import io.github.sameerbhilare.usersservice.ui.model.User;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UsersControllerITest {

    private final String TEST_EMAIL = "test@test.com";
    private final String TEST_PASSWORD = "123456789";

    @LocalServerPort
    private int port;

    private String userId;
    private String token;

    // log all data related to HTTP "Request"
    private final RequestLoggingFilter requestLoggingFilter = new RequestLoggingFilter();
    // to log only body data related to HTTP "Request"
    //private final RequestLoggingFilter requestLoggingFilter = new RequestLoggingFilter(LogDetail.BODY);
    // to log multiple data e.g. body and headers data related to HTTP "Request"
    //private final RequestLoggingFilter requestLoggingFilter = RequestLoggingFilter.with(LogDetail.BODY, LogDetail.HEADERS);

    // log all data related to HTTP "Response". Also has provision to log specific data similar to RequestLoggingFilter
    private final ResponseLoggingFilter responseLoggingFilter = new ResponseLoggingFilter();

    @BeforeAll
    void setUp() {
        // base URI for all http requests in this test class
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        // to log data related to HTTP request and response, common for all test methods in this class
        RestAssured.filters(requestLoggingFilter, responseLoggingFilter);

        /*
            "Request" specification is a feature that allows you to define a common set of configurations for all HTTP requests.
            e.g. setting common headers for all requests in this test class
            If requestSpecification is not used, we need to set common config (e.g. headers) in each Test method.
         */
        RestAssured.requestSpecification = new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                //.addFilter(new RequestLoggingFilter())    // another way to add filters instead of RestAssured.filters() above
                //.addFilter(new ResponseLoggingFilter())   // another way to add filters instead of RestAssured.filters() above
                .build();

        /*
            Response specification is used to define expectations for HTTP response.
            It allows you to specify expected response status code, expected response headers, response time, and other HTTP response details.
         */
        RestAssured.responseSpecification = new ResponseSpecBuilder()
                //.expectStatusCode(anyOf(is(200), is(201), is(204))) // not great practice as different test methods may expect different status
                .expectResponseTime(lessThan(2000L)) // min response time across all responses
                .build();
    }

    @Test
    @Order(1)
    @DisplayName("POST /users - Create User")
    void testCreateUser_whenValidDetailsProvided_returnsCreatedUser() {

        // Arrange
        User newUser = new User("Sameer","Bhilare", TEST_EMAIL, TEST_PASSWORD);

        // If we don't have access to User object (e.g. while testing third party API), we can also create Map<String, Object> manually.
        /*
        Map<String, Object> newUser = new HashMap<>();
        newUser.put("firstName", "Sameer");
        newUser.put("lastName", "Bhilare");
        newUser.put("email", TEST_EMAIL);
        newUser.put("password", TEST_PASSWORD);
         */

        // Act
        /**
         * Rest Assured supports BDD (Behavior-Driven Development), a style of writing tests that focuses on describing the behavior of the application.
         * Instead of just checking if something works, BDD focuses on making tests more understandable and descriptive.
         * Given - when - then => BDD (Behavior-Driven Development) style.
         *
         * The Fluent Interface pattern involves chaining methods to create more readable and expressive code.
         * Instead of breaking things into separate statements, everything flows smoothly from one call to the next.
         *
         */
        // WAY 1
        given()
                //.log().all() // to log everything related to "HTTP request". Alternatively We can choose to log specific data e.g. headers, URI, parameters, body, etc.
                .body(newUser)
        .when()
                .post("/users")
        .then()
                //.log().all() // to log everything related to "HTTP response". Alternatively We can choose to log specific data e.g. headers, URI, parameters, body, etc.
                .statusCode(201)
                .body("id", notNullValue())
                .body("firstName",equalTo(newUser.getFirstName()))
                .body("lastName",equalTo(newUser.getLastName()))
                .body("email",equalTo(newUser.getEmail()));

        // WAY 2
        /*
        // to extract response object and work with it
        UserRest createdUser =
                given()
                    .body(newUser)
                .when()
                    .post("/users")
                .then()
                    .extract()
                    .as(UserRest.class);
         */

        // WAY 3
        /*
        // Get response and work with response body, headers, cookies, status, etc.
        Response response =
            given()
                .body(newUser)
            .when()
                .post("/users")
            .then()
                .extract()
                .response();
         */
    }

    @Test
    @Order(2)
    @DisplayName("POST /login - Login User")
    void testLogin_whenValidCredentialsProvided_returnsTokenAndUserIdHeaders() {
        // Arrange
        Map<String, String> credentials = new HashMap<>();
        credentials.put("email", TEST_EMAIL);
        credentials.put("password", TEST_PASSWORD);

        // Act
        // Using WAY 3 as we need to store userId and token
        Response response =
                given()
                    .body(credentials)
                .when()
                    .post("/login"); // /login provided by spring security

        this.userId = response.header("userId");
        this.token = response.header("token");

        // Assert
        assertEquals(HttpStatus.OK.value(), response.statusCode());
        assertNotNull(userId);
        assertNotNull(token);
    }


    @Test
    @Order(3)
    @DisplayName("GET /users/{userId} - Get user details with auth token")
    void testGetUser_withValidAuthenticationToken_returnsUser() {
        given() // Arrange
                .pathParam("userId",this.userId)
                .header("Authorization", "Bearer " + this.token)
                //.auth().oauth2(this.token)
        .when() // Act
                .get("/users/{userId}")
        .then() // Assert
                .statusCode(HttpStatus.OK.value())
                .body("id",equalTo(this.userId))
                .body("email",equalTo(TEST_EMAIL))
                .body("firstName", notNullValue())
                .body("lastName", notNullValue());
    }


    @Test
    @Order(4)
    @DisplayName("GET /users/{userId} - Get user details without auth token")
    void testGetUser_withMissingAuthHeader_returnsForbidden() {
        given()
                .pathParam("userId", this.userId)
        .when()
                .get("/users/{userId}")
        .then()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

}
