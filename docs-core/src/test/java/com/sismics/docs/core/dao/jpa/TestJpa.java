package com.sismics.docs.core.dao.jpa;

import com.sismics.docs.BaseTransactionalTest;
import com.sismics.docs.core.dao.UserDao;
import com.sismics.docs.core.model.jpa.User;
import com.sismics.docs.core.util.TransactionUtil;
import com.sismics.docs.core.util.authentication.InternalAuthenticationHandler;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the persistance layer.
 * 
 * @author jtremeaux
 */
public class TestJpa extends BaseTransactionalTest {
    @Test
    public void testJpa() throws Exception {
        // Create a user
        UserDao userDao = new UserDao();
        User user = createUser("testJpa");

        TransactionUtil.commit();

        // Search a user by his ID
        user = userDao.getById(user.getId());
        Assert.assertNotNull(user);
        Assert.assertEquals("toto@docs.com", user.getEmail());

        // Authenticate using the database
        Assert.assertNotNull(new InternalAuthenticationHandler().authenticate("testJpa", "12345678"));

        // Delete the created user
        userDao.delete("testJpa", user.getId());
        TransactionUtil.commit();
    }

    @Test
    public void testCreateAndUpdateUser() throws Exception {
        UserDao userDao = new UserDao();
        User user = createUser("testCreateAndUpdate");

        TransactionUtil.commit();

        // Update user
        user.setEmail("updated@docs.com");
        userDao.update(user, "testCreateAndUpdate");
        TransactionUtil.commit();

        // Verify update
        User updatedUser = userDao.getById(user.getId());
        Assert.assertNotNull(updatedUser);
        Assert.assertEquals("updated@docs.com", updatedUser.getEmail());
    }

    @Test
    public void testDeleteNonExistentUser() throws Exception {
        UserDao userDao = new UserDao();

        // Attempt to delete a non-existent user
        userDao.delete("testDeleteNonExistent", "non-existent-id");
        TransactionUtil.commit();

        // No exception means success
    }

    @Test
    public void testAuthenticateInvalidUser() {
        InternalAuthenticationHandler authHandler = new InternalAuthenticationHandler();

        // Attempt to authenticate with invalid credentials
        Assert.assertNull(authHandler.authenticate("invalidUser", "wrongPassword"));
    }
}
