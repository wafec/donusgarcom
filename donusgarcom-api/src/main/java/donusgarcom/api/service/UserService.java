package donusgarcom.api.service;

import donusgarcom.api.common.exceptions.ServiceInvalidParamException;
import donusgarcom.api.database.domain.identity.UserDao;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dozer.Mapper;

import javax.inject.Inject;
import javax.xml.bind.annotation.XmlType;

public class UserService extends GenericService {
    static final Logger log = LogManager.getLogger(UserService.class);

    @Inject
    Mapper mapper;

    UserDao userDao;

    @Inject
    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    public UserGenericView createUser(NewUser newUser) {
        if (newUser != null && !userExists(newUser.name)) {
            checkString(newUser.name);
            checkString(newUser.pass);
            checkString(newUser.role);
            checkPassword(newUser.pass);
            UserDao.User user = mapper.map(newUser, UserDao.User.class);
            user.pass = generateHashPassword(user.pass);
            userDao.insert(user);
            userDao.commit();
            log.debug("User created: '" + new ReflectionToStringBuilder(user).toString() + "'");
            return mapper.map(user, UserGenericView.class);
        } else {
            throw new ServiceInvalidParamException();
        }
    }

    void checkPassword(String pass) {
        if (pass.length() < 6 || pass.length() > 16) {
            throw new ServiceInvalidParamException();
        }
    }

    String generateHashPassword(String pass) {
        return DigestUtils.sha256Hex(pass);
    }

    boolean userExists(String name) {
        return userDao.getByName(name) != null;
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

        public String getName() {
            return name;
        }

        public String getPass() {
            return pass;
        }

        public String getRole() {
            return role;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setPass(String pass) {
            this.pass = pass;
        }

        public void setRole(String role) {
            this.role = role;
        }
    }

    @XmlType(name="")
    public static class UserGenericView extends GenericView { }
}
