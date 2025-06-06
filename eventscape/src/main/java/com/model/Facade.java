package com.model;

import java.util.Date;
import java.util.List;

public class Facade {
    private static Facade instance;
    private final UserList userList;

    private Facade() {
        userList = UserList.getInstance();
    }

    public static Facade getInstance() {
        if (instance == null) {
            instance = new Facade();
        }
        return instance;
    }

    public List<User> getAllUsers() {
        return userList.getUsers();
    }

    public boolean addUser(String userName, String firstName, String lastName, String email, String phoneNumber, Date birthDate, String passwordHash) {
       return UserList.getInstance().addUser(userName, firstName, lastName, email, phoneNumber, birthDate, passwordHash);
    }

    public User findUser(String username) {
        return userList.getUserByUsername(username);
    }

    public boolean removeUser(String username) {
        User user = findUser(username);
        if (user != null) {
            userList.removeUser(user);
            return true;
        }
        return false;
    }

     public User login(String username, String password) {
        User user = userList.getUserByUsername(username);
        if (user != null && user.getPasswordHash().equals(password)) {
            return user;
        }
        return null;
    }

    public void logout() {
        UserList.getInstance().save();
    }
}
