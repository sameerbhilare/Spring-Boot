package io.github.sameerbhilare.junit.io;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
public class UsersRepositoryTest {

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private UsersRepository usersRepository;

    private final String userId1 = UUID.randomUUID().toString();
    private final String userId2 = UUID.randomUUID().toString();
    private final String email1 = "test@test.com";
    private final String email2 = "test2@test.com";

    @BeforeEach
    void setup() {
        // Creating first user
        UserEntity userEntity = new UserEntity();
        userEntity.setUserId(userId1);
        userEntity.setEmail(email1);
        userEntity.setEncryptedPassword("12345678");
        userEntity.setFirstName("Sameer");
        userEntity.setLastName("Bhilare");
        testEntityManager.persistAndFlush(userEntity);

        // Creating second user
        UserEntity userEntity2 = new UserEntity();
        userEntity2.setUserId(userId2);
        userEntity2.setEmail(email2);
        userEntity2.setEncryptedPassword("abcdefg1");
        userEntity2.setFirstName("Indra");
        userEntity2.setLastName("Raje");
        testEntityManager.persistAndFlush(userEntity2);
    }

    @Test
    void testFindByEmail_whenGivenCorrectEmail_returnsUserEntity() {
        // Act
        UserEntity storedUser = usersRepository.findByEmail(email1);

        // Assert
        assertEquals(email1, storedUser.getEmail(),
                "The returned email address does not match the expected value");
    }

    @Test
    void testFindByUserId_whenGivenCorrectUserId_returnsUserEntity() {
        // Act
        UserEntity storedUser = usersRepository.findByUserId(userId2);

        // Assert
        assertNotNull(storedUser,
                "UserEntity object should not be null");
        assertEquals(userId2, storedUser.getUserId(),
                "Returned userId does not much expected value");
    }

    @Test
    void testFindUsersWithEmailEndsWith_whenGiveEmailDomain_returnsUsersWithGivenDomain() {
        // Arrange
        UserEntity userEntity = new UserEntity();
        userEntity.setUserId(UUID.randomUUID().toString());
        userEntity.setEmail("test@gmail.com");
        userEntity.setEncryptedPassword("123456789");
        userEntity.setFirstName("Heramb");
        userEntity.setLastName("J");
        testEntityManager.persistAndFlush(userEntity);

        String emailDomainName = "@gmail.com";

        // Act
        List<UserEntity> users = usersRepository.findUsersWithEmailEndingWith(emailDomainName);

        // Assert
        assertEquals(1, users.size(),
                "There should be one user in the list");
        assertTrue(users.get(0).getEmail().endsWith(emailDomainName),
                "User's email does not end with target domain name");
    }
}
