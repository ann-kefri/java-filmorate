package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.mappers.UserRowMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserDbStorage.class, UserRowMapper.class})
class UserDbStorageTest {

    private final UserDbStorage userStorage;

    @Test
    void testFindUserById() {
        User user = new User();
        user.setEmail("test@test.com");
        user.setLogin("testlogin");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        User created = userStorage.create(user);

        Optional<User> userOptional = userStorage.getById(created.getId());

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(u ->
                        assertThat(u).hasFieldOrPropertyWithValue("id", created.getId())
                );
    }

    @Test
    void testCreateUser() {
        User user = new User();
        user.setEmail("new@test.com");
        user.setLogin("newlogin");
        user.setName("New User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User created = userStorage.create(user);

        assertThat(created)
                .hasFieldOrPropertyWithValue("email", "new@test.com")
                .hasFieldOrPropertyWithValue("login", "newlogin")
                .hasFieldOrPropertyWithValue("name", "New User")
                .hasFieldOrPropertyWithValue("birthday", LocalDate.of(1990, 1, 1));
        assertThat(created.getId()).isPositive();
    }

    @Test
    void testUpdateUser() {
        User user = new User();
        user.setEmail("update@test.com");
        user.setLogin("updatelogin");
        user.setName("Update User");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        User created = userStorage.create(user);

        created.setName("Updated Name");
        created.setEmail("updated@test.com");
        User updated = userStorage.update(created);

        assertThat(updated)
                .hasFieldOrPropertyWithValue("id", created.getId())
                .hasFieldOrPropertyWithValue("name", "Updated Name")
                .hasFieldOrPropertyWithValue("email", "updated@test.com");
    }

    @Test
    void testGetAllUsers() {
        User user = new User();
        user.setEmail("all@test.com");
        user.setLogin("alllogin");
        user.setName("All User");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        userStorage.create(user);

        List<User> users = userStorage.getAll();

        assertThat(users).isNotEmpty();
    }

    @Test
    void testDeleteUser() {
        User user = new User();
        user.setEmail("delete@test.com");
        user.setLogin("deletelogin");
        user.setName("Delete User");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        User created = userStorage.create(user);

        userStorage.delete(created.getId());
        Optional<User> found = userStorage.getById(created.getId());

        assertThat(found).isEmpty();
    }

    @Test
    void testAddFriend() {
        User user1 = new User();
        user1.setEmail("user1@test.com");
        user1.setLogin("user1login");
        user1.setName("User One");
        user1.setBirthday(LocalDate.of(1990, 1, 1));
        User created1 = userStorage.create(user1);

        User user2 = new User();
        user2.setEmail("user2@test.com");
        user2.setLogin("user2login");
        user2.setName("User Two");
        user2.setBirthday(LocalDate.of(1992, 2, 2));
        User created2 = userStorage.create(user2);

        userStorage.addFriend(created1.getId(), created2.getId());
        List<User> friends = userStorage.getFriends(created1.getId());

        assertThat(friends).hasSize(1);
        assertThat(friends.get(0))
                .hasFieldOrPropertyWithValue("id", created2.getId());
    }

    @Test
    void testRemoveFriend() {
        User user1 = new User();
        user1.setEmail("user1@test.com");
        user1.setLogin("user1login");
        user1.setName("User One");
        user1.setBirthday(LocalDate.of(1990, 1, 1));
        User created1 = userStorage.create(user1);

        User user2 = new User();
        user2.setEmail("user2@test.com");
        user2.setLogin("user2login");
        user2.setName("User Two");
        user2.setBirthday(LocalDate.of(1992, 2, 2));
        User created2 = userStorage.create(user2);

        userStorage.addFriend(created1.getId(), created2.getId());
        assertThat(userStorage.getFriends(created1.getId())).hasSize(1);

        userStorage.removeFriend(created1.getId(), created2.getId());
        assertThat(userStorage.getFriends(created1.getId())).isEmpty();
    }
}