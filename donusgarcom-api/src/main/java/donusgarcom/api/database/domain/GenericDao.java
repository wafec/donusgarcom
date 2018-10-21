package donusgarcom.api.database.domain;

import donusgarcom.api.database.util.DbManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public abstract class GenericDao <T extends GenericDao.GenericData> extends CoreDao {
    static final Logger log = LogManager.getLogger(GenericDao.class);

    protected abstract String getTableName();
    protected abstract Class<T> getClazz();

    DbManager dbManager;

    public GenericDao(DbManager dbManager) {
        this.dbManager = dbManager;
    }

    protected SqlField[] normalizeSubManagedSqlFields(String prefix, SqlField[] subSqlFields) {
        SqlField[] newSubManagedFields = new SqlField[subSqlFields.length];
        for (int i = 0; i < subSqlFields.length; i++) {
            newSubManagedFields[i] = subSqlFields[i].clone();
            newSubManagedFields[i].name = prefix + newSubManagedFields[i].name;
        }
        return newSubManagedFields;
    }

    protected SqlField[] unionOfManagedSqlFields(SqlField[]... arrayOfSqlFields) {
        ArrayList<SqlField> listOfSqlFields = new ArrayList<>();
        for (SqlField[] sqlFields : arrayOfSqlFields) {
            listOfSqlFields.addAll(Arrays.asList(sqlFields));
        }
        return listOfSqlFields.toArray(new SqlField[listOfSqlFields.size()]);
    }

    public T get(int id) {
        Object[] data = { null };
        String sql = String.format("SELECT * FROM %s WHERE id = ?", getTableName());
        dbManager.executeQuery(sql, new SqlValue[] { new SqlValue(id, SqlFieldType.INT) }, resultSet -> {
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
        dbManager.executeQuery(sql, sqlValues, resultSet -> {
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
        dbManager.executeQuery(sql, sqlValues, resultSet -> {
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
        dbManager.enqueueBeginTransaction();
        runnable.run();
        dbManager.dequeueAndExecuteAll();
    }

    public void commit() {
        dbManager.dequeueAndExecuteAll();
    }

    Field getField(SqlField sqlField) throws NoSuchFieldException {
        if (sqlField.name.contains(".")) {
            String[] names = sqlField.name.split("\\.");
            Class current = getClazz();
            for (int i = 0; i < names.length - 1; i++) {
                String name = names[i];
                current = current.getField(name).getType();
            }
            return current.getField(names[names.length - 1]);
        } else {
            return getClazz().getField(sqlField.name);
        }
    }

    Object getInstance(SqlField sqlField, T newInstance, boolean instantiateSubs)
            throws NoSuchFieldException, IllegalAccessException, InstantiationException {
        if (sqlField.name.contains(".")) {
            String[] names = sqlField.name.split("\\.");
            Class current = getClazz();
            Object subInstance = newInstance;
            for (int i = 0; i < names.length - 1; i++) {
                String name = names[i];
                Object newSub = current.getField(name).get(subInstance);
                if (newSub == null) {
                    if (instantiateSubs) {
                        newSub = current.getField(name).getType().newInstance();
                    } else {
                        return null;
                    }
                }
                subInstance = newSub;
                current = current.getField(name).getType();
            }
            return subInstance;
        } else {
            return newInstance;
        }
    }

    T fromResultSet(ResultSet resultSet) {
        try {
            Class<T> clazz = getClazz();
            T newInstance = clazz.newInstance();

            Field idField = clazz.getField("id");
            idField.set(newInstance, resultSet.getInt("id"));

            for (SqlField sqlField : getManagedSqlFields()) {
                try {
                    // Field anyField = clazz.getField(sqlField.name);
                    Field anyField = getField(sqlField);
                    Object value = null;
                    switch (sqlField.fieldType) {
                        case INT:
                            value = resultSet.getInt(sqlField.getQualifiedName());
                            break;
                        case STRING:
                            value = resultSet.getString(sqlField.getQualifiedName());
                            break;
                        case DATE:
                            value = resultSet.getTimestamp(sqlField.getQualifiedName()).toLocalDateTime();
                            break;
                        case DOUBLE:
                            value = resultSet.getDouble(sqlField.getQualifiedName());
                            break;
                        case BOOLEAN:
                            value = resultSet.getBoolean(sqlField.getQualifiedName());
                            break;
                    }
                    // anyField.set(newInstance, value);
                    anyField.set(getInstance(sqlField, newInstance, true), value);
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

    SqlValue fromSqlFieldToSqlValue(SqlField sqlField, T data) {
        try {
            Class<T> clazz = getClazz();
            // Field anyField = clazz.getField(sqlField.name);
            Field anyField = getField(sqlField);
            // Object value = anyField.get(data);
            Object value = anyField.get(getInstance(sqlField, data, false));
            return new SqlValue(value, sqlField.fieldType);
        } catch (NoSuchFieldException exception) {
            log.error(exception);
        } catch (IllegalAccessException exception) {
            log.error(exception);
        } catch (InstantiationException exception) {
            log.error(exception);
        }
        return null;
    }

    public void update(int id, final T data) {
        String sql = "UPDATE " + getTableName() + " SET ";
        String sqlSets = String.join(", ", Arrays.asList(getManagedSqlFields()).stream().map(sqlField -> {
                return "" + sqlField.getQualifiedName() + " = ?";
        }).filter(s -> !s.isEmpty()).collect(Collectors.toList()));
        List<SqlValue> listOfSqlValues = new ArrayList<>(Arrays.asList(getManagedSqlFields()).stream().map(sqlField -> {
            return fromSqlFieldToSqlValue(sqlField, data);
        }).filter(s -> s != null).collect(Collectors.toList()));
        sql = sql + sqlSets + " WHERE id = ?";
        listOfSqlValues.add(new SqlValue(id, SqlFieldType.INT));
        dbManager.enqueueExecuteUpdate(sql, listOfSqlValues.toArray(new SqlValue[listOfSqlValues.size()]));
    }

    public void delete(int id) {
        String sql = String.format("DELETE FROM %s WHERE id = ?", getTableName());
        dbManager.enqueueExecuteUpdate(sql, new SqlValue[] { new SqlValue(id, SqlFieldType.INT) });
    }

    public T insert(final T data) {
        int nextId = getNextId();
        data.id = nextId;
        List<SqlField> listOfSqlField = new ArrayList<>(Arrays.asList(getManagedSqlFields()));
        listOfSqlField.add(new SqlField("id", SqlFieldType.INT));
        String sql = "INSERT INTO " + getTableName() + " (";
        String sqlFieldNames = String.join(", ", listOfSqlField.stream().map(sqlField -> {
            return sqlField.getQualifiedName();
        }).collect(Collectors.toList()));
        sql = sql + sqlFieldNames + ") VALUES (";
        String sqlValues = String.join(", ", listOfSqlField.stream().map(sqlField -> {
            return "?";
        }).filter(s -> !s.isEmpty()).collect(Collectors.toList()));
        List<SqlValue> listOfSqlValues = new ArrayList<>(listOfSqlField.stream().map(sqlField -> {
            return fromSqlFieldToSqlValue(sqlField, data);
        }).filter(s -> s != null).collect(Collectors.toList()));
        sql = sql + sqlValues + ")";
        dbManager.enqueueExecuteUpdate(sql, listOfSqlValues.toArray(new SqlValue[listOfSqlField.size()]));
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
        dbManager.executeQuery("SELECT MAX(id) FROM " + getTableName(), new SqlValue[] {}, consumer);

        return res[0];
    }

    public static class GenericData extends CoreData {
        public int id;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
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
}
