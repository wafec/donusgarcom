package donusgarcom.api.database.domain.identity;

import donusgarcom.api.database.domain.GenericDao;
import donusgarcom.api.database.util.DbManager;

import javax.inject.Inject;

public class PersonaDao extends GenericDao<PersonaDao.Persona> {
    @Inject
    public PersonaDao(DbManager dbManager) {
        super(dbManager);
    }

    @Override
    public String getTableName() {
        return "personas";
    }

    @Override
    public Class<Persona> getClazz() {
        return Persona.class;
    }

    @Override
    public SqlField[] getManagedSqlFields() {
        return unionOfManagedSqlFields(
            new SqlField[] {
                    new SqlField("firstName", SqlFieldType.STRING),
                    new SqlField("lastName", SqlFieldType.STRING),
                    new SqlField("userId", SqlFieldType.INT)
            }
        );
    }

    public static class Persona extends GenericDao.GenericData {
        public String firstName;
        public String lastName;
        public int userId;

        public void setFirtName(String firtName) {
            this.firstName = firstName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public void setUserId(int userId) {
            this.userId = userId;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public int getUserId() {
            return userId;
        }
    }
}
