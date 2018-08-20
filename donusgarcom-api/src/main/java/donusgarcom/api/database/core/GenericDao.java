package donusgarcom.api.database.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class GenericDao <T extends GenericDao.GenericData> {
    static final Logger log = LogManager.getLogger(GenericDao.class);

    protected abstract SqlField[] getManagedSqlFields();
    protected abstract String getTableName();
    protected abstract Class<T> getClazz();

    SqlManager sqlManager;

    public GenericDao(SqlManager sqlManager) {
        this.sqlManager = sqlManager;
    }

    public T get(int id) {
        String sql = String.format("SELECT * FROM % WHERE id = %", getTableName(), id);
        ResultSet resultSet = sqlManager.executeQuery(sql);
        if (resultSet != null) {
            try {
                if (resultSet.next()) {
                    T data = fromResultSet(resultSet);
                    return data;
                }
            } catch (SQLException exception) {
                log.error(exception);
            }
        }
        return null;
    }

    public List<T> select(String whereSql) {
        String sql = String.format("SELECT * FROM % WHERE " + whereSql, getTableName());
        ResultSet resultSet = sqlManager.executeQuery(sql);
        if (resultSet != null) {
            try {
                ArrayList<T> res = new ArrayList<>();
                while (resultSet.next()) {
                    res.add(fromResultSet(resultSet));
                }
                return res;
            } catch (SQLException exception) {
                log.error(exception);
            }
        }
        return null;
    }

    public int count(String whereSql) {
        String sql = String.format("SELECT COUNT(*) as counting FROM % WHERE " + whereSql, getTableName());
        ResultSet resultSet = sqlManager.executeQuery(sql);
        if (resultSet != null) {
            try {
                if (resultSet.next()) {
                    return resultSet.getInt("counting");
                }
            } catch (SQLException exception) {
                log.error(exception);
            }
        }
        log.warn("ResultSet returned null for counting registers.");
        return 0;
    }

    public void doTransaction(Runnable runnable) {
        sqlManager.enqueueBeginTransaction();
        runnable.run();
        sqlManager.dequeueAndExecuteAll();
    }

    public void commit() {
        sqlManager.dequeueAndExecuteAll();
    }

    T fromResultSet(ResultSet resultSet) {
        try {
            Class<T> clazz = getClazz();
            T newInstance = clazz.newInstance();

            Field idField = clazz.getField("id");
            idField.set(newInstance, resultSet.getInt("id"));

            for (SqlField sqlField : getManagedSqlFields()) {
                try {
                    Field anyField = clazz.getField(sqlField.name);
                    Object value = null;
                    switch (sqlField.fieldType) {
                        case INT:
                            value = resultSet.getInt(sqlField.name);
                            break;
                        case STRING:
                            value = resultSet.getString(sqlField.name);
                            break;
                    }
                    anyField.set(newInstance, value);
                } catch (NoSuchFieldException exception) {
                    log.error(exception);
                }
            }
        } catch (InstantiationException | IllegalAccessException | NoSuchFieldException exception) {
            log.error(exception);
        } catch (SQLException exception) {
            log.error(exception);
        }
        return null;
    }

    public void update(int id, T data) {
        String sql = "UPDATE " + getTableName() + " SET ";
        String sqlSets = String.join(", ", Arrays.asList(getManagedSqlFields()).stream().map(sqlField -> {
            try {
                Class<T> clazz = getClazz();
                Field anyField = clazz.getField(sqlField.name);
                Object value = anyField.get(data);
                return "" + sqlField.name + " = " + getFormattedValue(value, sqlField.fieldType);
            } catch (NoSuchFieldException exception) {
                log.error(exception);
            } catch (IllegalAccessException exception) {
                log.error(exception);
            }
            return "";
        }).filter(s -> !s.isEmpty()).collect(Collectors.toList()));
        sql = sql + sqlSets + " WHERE id = " + id;
        sqlManager.enqueueExecuteUpdate(sql);
    }

    String getFormattedValue(Object value, SqlFieldType fieldType) {
        if (value == null)
            return "null";
        switch (fieldType) {
            case INT:
                return String.valueOf((Integer) value);
            case STRING:
                return "\"" + value.toString() + "\"";
        }
        return value.toString();
    }

    public void delete(int id) {
        String sql = String.format("DELETE FROM % WHERE id = %", getTableName(), id);
        sqlManager.enqueueExecuteUpdate(sql);
    }

    public T insert(T data) {
        int nextId = getNextId();
        data.id = nextId;
        List<SqlField> listOfSqlField = Arrays.asList(getManagedSqlFields());
        listOfSqlField.add(new SqlField("id", SqlFieldType.INT));
        String sql = "INSERT INTO " + getTableName() + " (";
        String sqlFieldNames = String.join(", ", listOfSqlField.stream().map(sqlField -> {
            return sqlField.name;
        }).collect(Collectors.toList()));
        sql = sql + sqlFieldNames + ") VALUES (";
        String sqlValues = String.join(", ", listOfSqlField.stream().map(sqlField -> {
            try {
                Class<T> clazz = getClazz();
                Field anyField = clazz.getField(sqlField.name);
                Object value = anyField.get(data);
                return getFormattedValue(value, sqlField.fieldType);
            } catch (NoSuchFieldException exception) {
                log.error(exception);
            } catch (IllegalAccessException exception) {
                log.error(exception);
            }
            return "";
        }).filter(s -> !s.isEmpty()).collect(Collectors.toList()));
        sql = sql + sqlValues + ")";
        sqlManager.enqueueExecuteUpdate(sql);
        return data;
    }

    int getNextId() {
        ResultSet resultSet = sqlManager.executeQuery("SELECT MAX(id) FROM " + getTableName());
        try {
            if (resultSet != null && resultSet.next()) {
                int currentMaxId = resultSet.getInt(0);
                return currentMaxId + 1;
            }
        } catch (SQLException exception) {
            log.error(exception);
        }
        return 1000;
    }

    public static class GenericData {
        int id;

        public int getId() {
            return id;
        }
    }

    public static class SqlField {
        public String name;
        public SqlFieldType fieldType;

        public SqlField(String name, SqlFieldType fieldType) {
            this.name = name;
            this.fieldType = fieldType;
        }
    }

    public enum SqlFieldType {
        INT,
        STRING,
        DATE
    }
}
