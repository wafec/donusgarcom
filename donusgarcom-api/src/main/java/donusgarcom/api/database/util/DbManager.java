package donusgarcom.api.database.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import donusgarcom.api.database.domain.GenericDao;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.function.Consumer;

public class DbManager {
    static final Logger log = LogManager.getLogger(DbManager.class);

    ArrayList<GenericSqlOperation> sqlOperations = new ArrayList<>();
    String driver;
    String url;
    String user;
    String password;

    protected DbManager() {

    }

    public DbManager(String driver, String url, String user, String password) {
        setConnectionParameters(driver, url, user, password);
    }

    protected final void setConnectionParameters(String driver, String url, String user, String password) {
        this.driver = driver;
        this.url = url;
        this.user = user;
        this.password = password;
    }

    protected Connection getConnection() {
        try {
            Class.forName(driver);
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException exception) {
            log.error(exception);
        } catch (ClassNotFoundException exception) {
            log.error(exception);
        }
        return null;
    }

    void setValuesOnPreparedStatement(PreparedStatement preparedStatement, GenericDao.SqlValue[] sqlValues) {
        for (int i = 0; i < sqlValues.length; i++) {
            try {
                GenericDao.SqlValue sqlValue = sqlValues[i];
                switch (sqlValue.fieldType) {
                    case INT:
                        preparedStatement.setInt(i + 1, (Integer) sqlValue.value);
                        break;
                    case STRING:
                        preparedStatement.setString(i + 1, (String) sqlValue.value);
                        break;
                    case DATE:
                        Timestamp timestamp = Timestamp.valueOf((LocalDateTime) sqlValue.value);
                        preparedStatement.setTimestamp(i + 1, timestamp);
                        break;
                }
            } catch (SQLException exception) {
                log.error(exception);
            }
        }
    }

    public void executeQuery(String sql, GenericDao.SqlValue[] sqlValues, Consumer<ResultSet> consumer) {
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            setValuesOnPreparedStatement(statement, sqlValues);
            log.debug("Executing SQL: " + sql);
            ResultSet resultSet = statement.executeQuery();
            consumer.accept(resultSet);
            connection.close();
            connection = null;
            statement = null;
        } catch (SQLException exception) {
            log.error(exception);
        } finally {
            if (connection != null || statement != null) {
                try {
                    connection.close();
                } catch (SQLException exception) {
                    log.error(exception);
                }
            }
        }
    }

    public void enqueueExecuteUpdate(String sql, GenericDao.SqlValue[] sqlValues) {
        sqlOperations.add(new SqlStatementOperation(SqlOperationType.EXECUTE_UPDATE, sql, sqlValues));
    }

    public void enqueueBeginTransaction() {
        sqlOperations.add(new GenericSqlOperation(SqlOperationType.BEGIN_TRANSACTION));
    }

    public void enqueueEndTransaction() {
        sqlOperations.add(new GenericSqlOperation(SqlOperationType.END_TRANSACTION));
    }

    public void dequeueAndExecuteAll() {
        ArrayList<SqlList> listOfSqlList = SqlList.createListOfSqlList(sqlOperations);
        ArrayList<SqlList> listOfNonExecutedSqlList = new ArrayList<>();
        for (int i = 0; i < listOfSqlList.size(); i++) {
            SqlList sqlList = listOfSqlList.get(i);
            if (!execute(sqlList)) {
                listOfNonExecutedSqlList.add(sqlList);
            }
        }
        sqlOperations.clear();
        int notExecuted = listOfNonExecutedSqlList.stream().mapToInt(l -> l.sqlStatementOperations.size()).sum();
        if (notExecuted > 0)
            log.warn(String.format("A list of %d statements where not executed due to a db error", notExecuted));
    }

    public boolean execute(SqlList sqlList) {
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = getConnection();
            connection.setAutoCommit(!sqlList.isAtomic);
            for (int i = 0; i < sqlList.sqlStatementOperations.size(); i++) {
                SqlStatementOperation statementOperation = sqlList.sqlStatementOperations.get(i);
                statement = connection.prepareStatement(statementOperation.statement);
                setValuesOnPreparedStatement(statement, statementOperation.sqlValues);
                switch (statementOperation.operation) {
                    case EXECUTE_UPDATE:
                        log.debug("Executing update: " + statementOperation.statement);
                        statement.executeUpdate();
                        break;
                }
            }
            if (sqlList.isAtomic)
                connection.commit();
            connection.close();
            connection = null;
            statement = null;
            return true;
        } catch (SQLException exception) {
            log.error(exception);
        } finally {
            if (connection != null && sqlList.isAtomic) {
                try {
                    connection.rollback();
                } catch (SQLException exception) {
                    log.error(exception);
                }
            }
            if (connection != null || statement != null) {
                try {
                    connection.close();
                } catch (SQLException exception) {
                    log.error(exception);
                }
            }
        }
        return false;
    }

    protected DbConfig getDbConfigFromStream(InputStream inputStream) {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try {
            DbConfig dbConfig = mapper.readValue(inputStream, DbConfig.class);
            return dbConfig;
        } catch (IOException exception) {
            log.error(exception);
        }
        return null;
    }

    protected static class DbConfig {
        public String driver;
        public String url;
        public String user;
        public String password;
    }

    static class SqlList {
        public boolean isAtomic;
        public ArrayList<SqlStatementOperation> sqlStatementOperations;

        public SqlList() { }

        public SqlList(boolean isAtomic, ArrayList<SqlStatementOperation> sqlStatementOperations) {
            this.isAtomic = isAtomic;
            this.sqlStatementOperations = sqlStatementOperations;
        }

        public static ArrayList<SqlList> createListOfSqlList(ArrayList<GenericSqlOperation> sqlOperations) {
            ArrayList<SqlList> listOfSqlList = new ArrayList<>();
            ArrayList<SqlStatementOperation> statementOperations = new ArrayList<>();
            for (int i = 0; i < sqlOperations.size(); i++) {
                GenericSqlOperation operation = sqlOperations.get(i);
                switch (operation.operation) {
                    case BEGIN_TRANSACTION:
                        listOfSqlList.add(new SqlList(false, statementOperations));
                        statementOperations = new ArrayList<>();
                        break;
                    case END_TRANSACTION:
                        listOfSqlList.add(new SqlList(true, statementOperations));
                        statementOperations = new ArrayList<>();
                        break;
                    case EXECUTE_UPDATE:
                        statementOperations.add((SqlStatementOperation) operation);
                        break;
                }
            }
            if (statementOperations.size() > 0) {
                listOfSqlList.add(new SqlList(false, statementOperations));
            }
            return listOfSqlList;
        }
    }

    static class GenericSqlOperation {
        public SqlOperationType operation;

        public GenericSqlOperation(SqlOperationType operation) {
            this.operation = operation;
        }
    }

    static class SqlStatementOperation extends GenericSqlOperation {
        public String statement;
        public GenericDao.SqlValue[] sqlValues;

        public SqlStatementOperation(SqlOperationType operation, String statement, GenericDao.SqlValue[] sqlValues) {
            super(operation);
            this.statement = statement;
            this.sqlValues = sqlValues;
        }
    }

    enum SqlOperationType {
        EXECUTE_UPDATE,
        BEGIN_TRANSACTION,
        END_TRANSACTION
    }
}
