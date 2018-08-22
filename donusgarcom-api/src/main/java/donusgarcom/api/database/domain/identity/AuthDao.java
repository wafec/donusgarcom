package donusgarcom.api.database.domain.identity;

import donusgarcom.api.database.domain.GenericDao;
import donusgarcom.api.database.util.DbManager;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.List;

public class AuthDao extends GenericDao<AuthDao.Auth> {
    @Inject
    public AuthDao(DbManager dbManager) {
        super(dbManager);
    }

    @Override
    public String getTableName() {
        return "auths";
    }

    @Override
    public Class<AuthDao.Auth> getClazz() {
        return AuthDao.Auth.class;
    }

    @Override
    public SqlField[] getManagedSqlFields() {
        return new SqlField[] {
                new SqlField("userId", SqlFieldType.INT),
                new SqlField("token", SqlFieldType.STRING),
                new SqlField("creationDate", SqlFieldType.DATE),
                new SqlField("expirationDate", SqlFieldType.DATE)
        };
    }

    public AuthDao.Auth getByUserId(int userId) {
        List<AuthDao.Auth> list = select("userId = ?", new SqlValue[] {
                new SqlValue(userId, SqlFieldType.INT)
        });
        if (list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    public static class Auth extends GenericDao.GenericData {
        public int userId;
        public String token;
        public LocalDateTime creationDate;
        public LocalDateTime expirationDate;

        public Auth() { }

        public Auth(int userId, String token, LocalDateTime creationDate, LocalDateTime expirationDate) {
            this.userId = userId;
            this.token = token;
            this.creationDate = creationDate;
            this.expirationDate = expirationDate;
        }
    }
}
