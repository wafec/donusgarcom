package donusgarcom.api.database.domain;

import donusgarcom.api.database.core.GenericDao;
import donusgarcom.api.database.core.SqlManager;

public class UserDao extends GenericDao<UserDao.User> {
    public UserDao(SqlManager sqlManager) {
        super(sqlManager);
    }

    @Override
    protected String getTableName() {
        return "api.user";
    }

    @Override
    protected SqlField[] getManagedFieldNames() {
        return new SqlField[] {
            new SqlField("name", SqlFieldType.STRING),
            new SqlField("pass", SqlFieldType.STRING),
            new SqlField("role", SqlFieldType.STRING)
        };
    }

    @Override
    protected Class<UserDao.User> getClazz() {
        return UserDao.User.class;
    }

    public static class User extends GenericDao.GenericData {
        String name;
        String pass;
        String role;
    }
}
