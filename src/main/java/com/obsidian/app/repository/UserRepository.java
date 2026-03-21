package com.obsidian.app.repository;

import com.obsidian.app.models.User;
import com.obsidian.core.database.orm.repository.BaseRepository;
import com.obsidian.core.di.annotations.Repository;

/**
 * Handles all database queries for the User model.
 * Injected automatically into any class that declares it with @Inject.
 *
 * @Repository registers this class in Obsidian's dependency injection container.
 */
@Repository
public class UserRepository extends BaseRepository<User>
{
    public UserRepository() {
        super(User.class);
    }

    /**
     * Finds a user by their primary key.
     * Used by AppUserDetailsService to reload the user from the session.
     *
     * @param id the user's primary key
     * @return the matching User, or null if not found
     */
    public User findById(Object id) {
        return findBy("id", id);
    }

    /**
     * Finds a user by their username.
     * Used by AppUserDetailsService during login.
     *
     * @param username the username to search for
     * @return the matching User, or null if not found
     */
    public User findByUsername(String username) {
        return findBy("username", username);
    }

    /**
     * Finds a user by their email address."
     *
     * @param email the email to search for
     * @return the matching User, or null if not found
     */
    public User findByEmail(String email) {
        return findBy("email", email);
    }

    /**
     * Creates and persists a new user with the given username and hashed password.
     * Role defaults to "USER".
     *
     * @param username       the username
     * @param hashedPassword the already-hashed password (use Auth.hashPassword() before calling)
     * @return the newly created User
     */
    public User create(String username, String hashedPassword)
    {
        User user = new User();
        user.set("username", username);
        user.set("password", hashedPassword);
        user.set("role",     "USER");
        user.saveIt();
        return user;
    }
}