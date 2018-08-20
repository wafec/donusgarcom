package donusgarcom.api.database.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public abstract class SqlManager {
    static final Logger log = LogManager.getLogger(SqlManager.class);

    ArrayList<GenericSqlOperation> sqlOperations = new ArrayList<>();

    protected abstract Connection getConnection();

    public ResultSet executeQuery(String sql) {
        Connection connection = null;
        Statement statement = null;
        try {
            connection = getConnection();
            statement = connection.createStatement();
            log.debug("Executing SQL: " + sql);
            ResultSet res = statement.executeQuery(sql);
            connection.close();
            connection = null;
            statement = null;
            return res;
        } catch (SQLException exception) {
            log.error(exception);
        } finally {
            if (connection == null || statement == null) {
                try {
                    connection.close();
                } catch (SQLException exception) {
                    log.error(exception);
                }
            }
        }
        return null;
    }

    public void enqueueExecuteUpdate(String sql) {
        sqlOperations.add(new SqlStatementOperation(SqlOperationType.EXECUTE_UPDATE, sql));
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
        log.warn(String.format("A list of % statements where not executed due to a db error",
                listOfNonExecutedSqlList.stream().mapToInt(l -> l.sqlStatementOperations.size()).sum()));
    }

    public boolean execute(SqlList sqlList) {
        Connection connection = null;
        Statement statement = null;
        try {
            connection = getConnection();
            connection.setAutoCommit(!sqlList.isAtomic);
            statement = connection.createStatement();
            for (int i = 0; i < sqlList.sqlStatementOperations.size(); i++) {
                SqlStatementOperation statementOperation = sqlList.sqlStatementOperations.get(i);
                switch (statementOperation.operation) {
                    case EXECUTE_UPDATE:
                        log.debug("Executing update: " + statementOperation.statement);
                        statement.executeUpdate(statementOperation.statement);
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
                        statementOperations.clear();
                        break;
                    case END_TRANSACTION:
                        listOfSqlList.add(new SqlList(true, statementOperations));
                        statementOperations.clear();
                        break;
                    case EXECUTE_UPDATE:
                        statementOperations.add((SqlStatementOperation) operation);
                        break;
                }
            }
            if (statementOperations.size() > 0) {
                listOfSqlList.add(new SqlList(false, statementOperations));
                statementOperations.clear();
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

        public SqlStatementOperation(SqlOperationType operation, String statement) {
            super(operation);
            this.statement = statement;
        }
    }

    enum SqlOperationType {
        EXECUTE_UPDATE,
        BEGIN_TRANSACTION,
        END_TRANSACTION
    }
}
