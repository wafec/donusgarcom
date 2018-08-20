package donusgarcom.api.service;

import donusgarcom.api.database.domain.AuthDao;
import donusgarcom.api.database.domain.UserDao;
import donusgarcom.api.service.exception.AuthenticationFailException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import java.util.Calendar;
import java.util.Date;

public class AuthService {
    static Logger log = LogManager.getLogger(AuthService.class);
    static String secretKey = "orangeJuiceIsSoGood";

    UserDao userDao;
    AuthDao authDao;

    @Inject
    public AuthService(UserDao userDao, AuthDao authDao) {
        this.userDao = userDao;
        this.authDao = authDao;
    }

    public AuthToken requestToken(AuthUser authUser) {
        if (isAuthentic(authUser)) {
            AuthToken authToken = new AuthToken(createToken(authUser));
            saveTokenToDatabase(authUser, authToken);
            return authToken;
        }
        throw new AuthenticationFailException();
    }

    String createToken(AuthUser authUser) {
        return Jwts.builder()
                .setSubject(authUser.username)
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .compact();
    }

    boolean isAuthentic(AuthUser authUser) {
        return userDao.authenticate(authUser.username, authUser.password);
    }

    void saveTokenToDatabase(AuthUser authUser, AuthToken authToken) {
        UserDao.User user = userDao.getByName(authUser.username);
        if (user != null) {
            Calendar calendarInstance = Calendar.getInstance();
            calendarInstance.setTime(new Date());
            calendarInstance.add(Calendar.HOUR_OF_DAY, 5);
            AuthDao.Auth auth = new AuthDao.Auth(
                    user.getId(),
                    authToken.token,
                    new Date(),
                    calendarInstance.getTime()
            );
            removeExistentTokenFromDatabase(user);
            authDao.insert(auth);
            authDao.commit();
        } else {
            log.warn("Strange event happening! An authenticated user has not been gathered from the database.");
        }
    }

    void removeExistentTokenFromDatabase(UserDao.User user) {
        AuthDao.Auth auth = authDao.getByUserId(user.getId());
        if (auth != null) {
            authDao.delete(auth.getId());
            authDao.commit();
        }
    }

    public static class AuthUser {
        public String username;
        public String password;
    }

    public static class AuthToken {
        public String token;

        public AuthToken(String token) {
            this.token = token;
        }
    }
}
