package donusgarcom.api.service;

import donusgarcom.api.database.domain.identity.AuthDao;
import donusgarcom.api.database.domain.identity.UserDao;
import donusgarcom.api.common.exceptions.AuthErrorException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.Date;

public class AuthService extends GenericService {
    static final Logger log = LogManager.getLogger(AuthService.class);
    static final String secretKey = "orangeJuiceIsSoGood";
    static final int tokenDurationInHours = 6;

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
        throw new AuthErrorException();
    }

    String createToken(AuthUser authUser) {
        String subject = authUser.username;
        subject += new Date().toString();
        return Jwts.builder()
                .setSubject(subject)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    boolean isAuthentic(AuthUser authUser) {
        return userDao.authenticate(authUser.username, authUser.password);
    }

    void saveTokenToDatabase(AuthUser authUser, AuthToken authToken) {
        UserDao.User user = userDao.getByName(authUser.username);
        if (user != null) {
            LocalDateTime current = LocalDateTime.now();
            AuthDao.Auth auth = new AuthDao.Auth(
                    user.id,
                    authToken.token,
                    current,
                    current.plusHours(tokenDurationInHours)
            );
            removeExistentTokenFromDatabase(user);
            authDao.insert(auth);
            authDao.commit();
        } else {
            log.warn("Strange event happening! An authenticated user has not been gathered from the database.");
        }
    }

    void removeExistentTokenFromDatabase(UserDao.User user) {
        AuthDao.Auth auth = authDao.getByUserId(user.id);
        if (auth != null) {
            authDao.delete(auth.id);
            authDao.commit();
        }
    }

    public static class AuthUser {
        public String username;
        public String password;

        public AuthUser() { }
    }

    public static class AuthToken {
        public String token;

        public AuthToken() { }

        public AuthToken(String token) {
            this.token = token;
        }
    }
}
