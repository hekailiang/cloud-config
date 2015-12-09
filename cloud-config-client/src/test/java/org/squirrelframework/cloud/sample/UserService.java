package org.squirrelframework.cloud.sample;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squirrelframework.cloud.annotation.RoutingKey;

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
    @RoutingKey("read-1")
    public List<User> findAllUsers() {
        return userDAO.findAllUsers();
    }

    @Transactional
    @RoutingKey("write")
    public List<User> findAllUsersAsWrite() {
        return userDAO.findAllUsers();
    }

}
