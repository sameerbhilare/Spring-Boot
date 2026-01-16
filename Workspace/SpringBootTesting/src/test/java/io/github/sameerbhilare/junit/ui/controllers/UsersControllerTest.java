package io.github.sameerbhilare.junit.ui.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.sameerbhilare.junit.service.UsersService;
import io.github.sameerbhilare.junit.shared.UserDto;
import io.github.sameerbhilare.junit.ui.request.UserDetailsRequestModel;
import io.github.sameerbhilare.junit.ui.response.UserRest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * @WebMvcTest - It provides a test environment for the UsersController class by setting up a narrow Spring context focused on only the web layer (controllers layer).
 *
 * @WebMvcTest is used for testing a specific slice of the application (typically web layer),
 * while @SpringBootTest loads the whole application context for more general tests.
 */
@WebMvcTest(controllers = {UsersController.class},
    excludeAutoConfiguration = {SecurityAutoConfiguration.class}
)
//@AutoConfigureMockMvc(addFilters = false) // Option 2: disable Spring security
public class UsersControllerTest {

    /**
     * MockMvc is a core part of the Spring Test framework used to test Spring MVC applications by performing full request handling
     * through mock request and response objects, without the need for a running server.
     * It primarily uses a fluent API to build and execute requests and then verify the results.
     */
    @Autowired
    MockMvc mockMvc;

    @MockitoBean // @MockBean is deprecated now.
    UsersService usersService;

    private UserDetailsRequestModel userDetailsRequestModel;

    @BeforeEach
    void setup() {
        userDetailsRequestModel = new UserDetailsRequestModel();
        userDetailsRequestModel.setFirstName("Sameer");
        userDetailsRequestModel.setLastName("Bhilare");
        userDetailsRequestModel.setEmail("test@test.com");
        userDetailsRequestModel.setPassword("12345678");
    }

    @Test
    @DisplayName("User can be created")
    void testCreateUser_whenValidUserDetailsProvided_returnsCreateUserDetails() throws Exception {

        // Arrange
        UserDto userDto = new ModelMapper().map(userDetailsRequestModel, UserDto.class);
        userDto.setUserId(UUID.randomUUID().toString());

        when(usersService.createUser(any(UserDto.class))).thenReturn(userDto);

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(userDetailsRequestModel));

        // Act
        MvcResult mvcResult = mockMvc.perform(requestBuilder).andReturn();
        String content = mvcResult.getResponse().getContentAsString();
        UserRest createdUser = new ObjectMapper().readValue(content, UserRest.class);

        // Assert
        assertEquals(userDetailsRequestModel.getFirstName(),
                createdUser.getFirstName(), "The returned user first name is most likely incorrect");

        assertEquals(userDetailsRequestModel.getLastName(),
                createdUser.getLastName(), "The returned user last name is incorrect");

        assertEquals(userDetailsRequestModel.getEmail(),
                createdUser.getEmail(), "The returned user email is incorrect");

        assertFalse(createdUser.getUserId().isEmpty(), "userId should not be empty");

    }

    @Test
    @DisplayName("First name is not empty")
    void testCreateUser_whenFirstNameIsNotProvided_returns400StatusCode() throws Exception {
        // Arrange
        userDetailsRequestModel.setFirstName("");

        RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(userDetailsRequestModel));

        // Act
        MvcResult mvcResult = mockMvc.perform(requestBuilder).andReturn();

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST.value(),
                mvcResult.getResponse().getStatus(),
                "Incorrect HTTP Status Code returned");
    }

    @Test
    @DisplayName("First name cannot be shorter than 2 characters")
    void testCreateUser_whenFirstNameIsOnlyOneCharacter_returns400StatusCode() throws Exception {
        // Arrange
        userDetailsRequestModel.setFirstName("a");

        RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/users")
                .content(new ObjectMapper().writeValueAsString(userDetailsRequestModel))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);

        // Act
        MvcResult result = mockMvc.perform(requestBuilder).andReturn();

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST.value(),
                result.getResponse().getStatus(), "HTTP Status code is not set to 400");
    }
}
