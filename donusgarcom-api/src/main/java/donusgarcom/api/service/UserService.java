package donusgarcom.api.service;

import donusgarcom.api.database.domain.UserDao;

import javax.inject.Inject;

public class UserService {
    UserDao userDao;

    @Inject
    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    public void createUser(NewUser newUser) {

    }

    public static class NewUser {
        public String name;
        public String pass;
        public String role;

        public NewUser() {

        }

        public NewUser(String name, String pass, String role) {
            this.name = name;
            this.pass = pass;
            this.role = role;
        }
    }
}
