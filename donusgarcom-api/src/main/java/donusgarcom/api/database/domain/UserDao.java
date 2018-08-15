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

    public static class Roles {
        Roles() { }

        public static final String ADMIN = "admin";
        public static final String USER = "user";
        public static final String OWNER = "owner";
        public static final String MANAGER = "manager";
        public static final String STAFF = "staff";
        public static final String CONSULTANT = "consultant";
    }
}
