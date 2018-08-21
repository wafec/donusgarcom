package donusgarcom.api.database.domain;

import donusgarcom.api.database.core.GenericDao;
import donusgarcom.api.database.core.SqlManager;

import java.util.List;

public class UserDao extends GenericDao<UserDao.User> {
    public UserDao(SqlManager sqlManager) {
        super(sqlManager);
    }

    @Override
    protected String getTableName() {
        return "users";
    }

    @Override
    protected SqlField[] getManagedSqlFields() {
        return new SqlField[] {
            new SqlField("name", SqlFieldType.STRING),
            new SqlField("pass", SqlFieldType.STRING),
            new SqlField("role", SqlFieldType.STRING)
        };
    }

    public boolean authenticate(String name, String pass) {
        return count("name = ? AND pass = ?", new SqlValue[] {
                new SqlValue(name, SqlFieldType.STRING),
                new SqlValue(pass, SqlFieldType.STRING)
        }) > 0;
    }

    public UserDao.User getByName(String name) {
        List<UserDao.User> list = select("name = ?", new SqlValue[] {
                new SqlValue(name, SqlFieldType.STRING)
        });
        if (list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    @Override
    protected Class<UserDao.User> getClazz() {
        return UserDao.User.class;
    }

    public static class User extends GenericDao.GenericData {
        public String name;
        public String pass;
        public String role;
    }
}
