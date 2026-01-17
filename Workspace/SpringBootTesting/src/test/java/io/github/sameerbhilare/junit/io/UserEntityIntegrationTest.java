package io.github.sameerbhilare.junit.io;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.UUID;

@DataJpaTest // enables auto-configuration only that is relevant to Data JPA tests. e.g. Entities, Repositories
public class UserEntityIntegrationTest {

    @Autowired
    TestEntityManager testEntityManager;    // Alternative to EntityManager for use in JPA tests.

    UserEntity userEntity;

    @BeforeEach
    void setup() {
        userEntity = new UserEntity();
        userEntity.setUserId(UUID.randomUUID().toString());
        userEntity.setFirstName("Sameer");
        userEntity.setLastName("Bhilare");
        userEntity.setEmail("test@test.com");
        userEntity.setEncryptedPassword("12345678");
    }

    @Test
    @DisplayName("Store valid User in database")
    void testUserEntity_whenValidUserDetailsProvided_shouldReturnStoredUserDetails() {
        // Act
        UserEntity storedUserEntity = testEntityManager.persistAndFlush(userEntity);

        // Assert
        Assertions.assertTrue(storedUserEntity.getId() > 0);
        Assertions.assertEquals(userEntity.getUserId(), storedUserEntity.getUserId());
        Assertions.assertEquals(userEntity.getFirstName(), storedUserEntity.getFirstName());
        Assertions.assertEquals(userEntity.getLastName(), storedUserEntity.getLastName());
        Assertions.assertEquals(userEntity.getEmail(), storedUserEntity.getEmail());
        Assertions.assertEquals(userEntity.getEncryptedPassword(), storedUserEntity.getEncryptedPassword());
    }
}
