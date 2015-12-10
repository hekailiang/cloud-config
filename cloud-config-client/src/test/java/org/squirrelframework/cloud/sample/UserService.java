package org.squirrelframework.cloud.sample;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squirrelframework.cloud.annotation.RoutingKey;
import org.squirrelframework.cloud.routing.RoutingKeyHolder;

import java.util.List;

/**
 * Created by kailianghe on 15/12/8.
 */
@Service
public class UserService {
    @Autowired
    private UserDAO userDAO;

    @Transactional
    @RoutingKey("write")
    public void insertUser(User user) {
        userDAO.insertUser(user);
    }

    @Transactional
    @RoutingKey(value = "read", recordRoutingKeys = true)
    public void insertUserAsRead(User user) {
        userDAO.insertUser(user);
    }

    @Transactional
    @RoutingKey(value = "read", recordRoutingKeys = true)
    public List<User> findAllUsers() {
        return userDAO.findAllUsers();
    }

    @Transactional
    @RoutingKey("write")
    public List<User> findAllUsersAsWrite() {
        return userDAO.findAllUsers();
    }

}
