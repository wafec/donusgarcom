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
        return "api.user";
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
        return count(String.format("name = '%' AND pass = '%'", name, pass)) > 0;
    }

    public UserDao.User getByName(String name) {
        List<UserDao.User> list = select(String.format("name = '%'", name));
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
        String name;
        String pass;
        String role;
    }
}
