package donusgarcom.api.database.domain;

public abstract class CoreDao {
    protected abstract SqlField[] getManagedSqlFields();

    public static class SqlField {
        public String name;
        public SqlFieldType fieldType;

        public SqlField(String name, SqlFieldType fieldType) {
            this.name = name;
            this.fieldType = fieldType;
        }

        public SqlField clone() {
           return new SqlField(name, fieldType);
        }

        public String getQualifiedName() {
            if (name == null)
                return null;
            return name.replace(".", "_");
        }
    }

    public enum SqlFieldType {
        INT,
        STRING,
        DATE,
        DOUBLE,
        BOOLEAN
    }

    public static class CoreData {

    }
}
