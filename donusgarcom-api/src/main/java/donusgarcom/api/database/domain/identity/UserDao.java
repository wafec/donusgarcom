package donusgarcom.api.database.domain.identity;

import donusgarcom.api.database.domain.GenericDao;
import donusgarcom.api.database.util.DbManager;

import javax.inject.Inject;
import java.util.List;

public class UserDao extends GenericDao<UserDao.User> {
    @Inject
    public UserDao(DbManager dbManager) {
        super(dbManager);
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
}
