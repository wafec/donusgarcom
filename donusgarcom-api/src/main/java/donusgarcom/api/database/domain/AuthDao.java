package donusgarcom.api.database.domain;

import donusgarcom.api.database.core.GenericDao;
import donusgarcom.api.database.core.SqlManager;

import java.util.Date;
import java.util.List;

public class AuthDao extends GenericDao<AuthDao.Auth> {
    public AuthDao(SqlManager sqlManager) {
        super(sqlManager);
    }

    @Override
    public String getTableName() {
        return "api.auth";
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
        List<AuthDao.Auth> list = select(String.format("userId = %", userId));
        if (list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    public static class Auth extends GenericDao.GenericData {
        public int userId;
        public String token;
        public Date creationDate;
        public Date expirationDate;

        public Auth() { }

        public Auth(int userId, String token, Date creationDate, Date expirationDate) {
            this.userId = userId;
            this.token = token;
            this.creationDate = creationDate;
            this.expirationDate = expirationDate;
        }
    }
}
