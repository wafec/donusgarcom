package donusgarcom.api.database.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Consumer;
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
        Object[] data = { null };
        String sql = String.format("SELECT * FROM %s WHERE id = ?", getTableName());
        sqlManager.executeQuery(sql, new SqlValue[] { new SqlValue(id, SqlFieldType.INT) }, resultSet -> {
            if (resultSet != null) {
                try {
                    if (resultSet.next()) {
                        data[0] = fromResultSet(resultSet);
                    }
                } catch (SQLException exception) {
                    log.error(exception);
                }
            }
        });

        return (T) data[0];
    }

    public List<T> select(String whereSql, SqlValue[] sqlValues) {
        String sql = String.format("SELECT * FROM %s WHERE " + whereSql, getTableName());
        ArrayList<T> res = new ArrayList<>();
        sqlManager.executeQuery(sql, sqlValues, resultSet -> {
            if (resultSet != null) {
                try {

                    while (resultSet.next()) {
                        res.add(fromResultSet(resultSet));
                    }
                } catch (SQLException exception) {
                    log.error(exception);
                }
            }
        });
        return res;
    }

    public int count(String whereSql, SqlValue[] sqlValues) {
        String sql = String.format("SELECT COUNT(*) as counting FROM %s WHERE " + whereSql, getTableName());
        final int[] res = { 0 };
        sqlManager.executeQuery(sql, sqlValues, resultSet -> {
            if (resultSet != null) {
                try {
                    if (resultSet.next()) {
                        res[0] = resultSet.getInt("counting");
                    }
                } catch (SQLException exception) {
                    log.error(exception);
                }
            }
        });
        return res[0];
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
                        case DATE:
                            value = resultSet.getTimestamp(sqlField.name).toLocalDateTime();
                            break;
                    }
                    anyField.set(newInstance, value);
                } catch (NoSuchFieldException exception) {
                    log.error(exception);
                }
            }
            return newInstance;
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
                return "" + sqlField.name + " = ?";
        }).filter(s -> !s.isEmpty()).collect(Collectors.toList()));
        List<SqlValue> listOfSqlValues = new ArrayList<>(Arrays.asList(getManagedSqlFields()).stream().map(sqlField -> {
            try {
                Class<T> clazz = getClazz();
                Field anyField = clazz.getField(sqlField.name);
                Object value = anyField.get(data);
                return new SqlValue(value, sqlField.fieldType);
            } catch (NoSuchFieldException exception) {
                log.error(exception);
            } catch (IllegalAccessException exception) {
                log.error(exception);
            }
            return null;
        }).filter(s -> s != null).collect(Collectors.toList()));
        sql = sql + sqlSets + " WHERE id = ?";
        listOfSqlValues.add(new SqlValue(id, SqlFieldType.INT));
        sqlManager.enqueueExecuteUpdate(sql, listOfSqlValues.toArray(new SqlValue[listOfSqlValues.size()]));
    }

    public void delete(int id) {
        String sql = String.format("DELETE FROM %s WHERE id = ?", getTableName());
        sqlManager.enqueueExecuteUpdate(sql, new SqlValue[] { new SqlValue(id, SqlFieldType.INT) });
    }

    public T insert(T data) {
        int nextId = getNextId();
        data.id = nextId;
        List<SqlField> listOfSqlField = new ArrayList<>(Arrays.asList(getManagedSqlFields()));
        listOfSqlField.add(new SqlField("id", SqlFieldType.INT));
        String sql = "INSERT INTO " + getTableName() + " (";
        String sqlFieldNames = String.join(", ", listOfSqlField.stream().map(sqlField -> {
            return sqlField.name;
        }).collect(Collectors.toList()));
        sql = sql + sqlFieldNames + ") VALUES (";
        String sqlValues = String.join(", ", listOfSqlField.stream().map(sqlField -> {
            return "?";
        }).filter(s -> !s.isEmpty()).collect(Collectors.toList()));
        List<SqlValue> listOfSqlValues = new ArrayList<>(listOfSqlField.stream().map(sqlField -> {
            try {
                Class<T> clazz = getClazz();
                Field anyField = clazz.getField(sqlField.name);
                Object value = anyField.get(data);
                return new SqlValue(value, sqlField.fieldType);
            } catch (NoSuchFieldException exception) {
                log.error(exception);
            } catch (IllegalAccessException exception) {
                log.error(exception);
            }
            return null;
        }).filter(s -> s != null).collect(Collectors.toList()));
        sql = sql + sqlValues + ")";
        sqlManager.enqueueExecuteUpdate(sql, listOfSqlValues.toArray(new SqlValue[listOfSqlField.size()]));
        return data;
    }

    int getNextId() {
        final int[] res = { 1000 };
        Consumer consumer = new Consumer<ResultSet>() {
            @Override
            public void accept(ResultSet resultSet) {
                try {
                    if (resultSet != null && resultSet.next()) {
                        int currentMaxId = resultSet.getInt(1);
                        res[0] = currentMaxId + 1;
                    }
                } catch (SQLException exception) {
                    log.error(exception);
                }
            }
        };
        sqlManager.executeQuery("SELECT MAX(id) FROM " + getTableName(), new SqlValue[] {}, consumer);

        return res[0];
    }

    public static class GenericData {
        public int id;
    }

    public static class SqlField {
        public String name;
        public SqlFieldType fieldType;

        public SqlField(String name, SqlFieldType fieldType) {
            this.name = name;
            this.fieldType = fieldType;
        }
    }

    public static class SqlValue {
        public Object value;
        public SqlFieldType fieldType;

        public SqlValue(Object value, SqlFieldType fieldType) {
            this.value = value;
            this.fieldType = fieldType;
        }
    }

    public enum SqlFieldType {
        INT,
        STRING,
        DATE
    }
}
