package io.edurt.datacap.condor;

import io.edurt.datacap.condor.manager.DatabaseManager;
import io.edurt.datacap.condor.manager.TableManager;
import io.edurt.datacap.condor.metadata.ColumnDefinition;
import io.edurt.datacap.condor.metadata.DatabaseDefinition;
import io.edurt.datacap.condor.metadata.RowDefinition;
import io.edurt.datacap.condor.metadata.TableDefinition;
import io.edurt.datacap.sql.SQLParser;
import io.edurt.datacap.sql.node.ColumnConstraint;
import io.edurt.datacap.sql.node.ConstraintType;
import io.edurt.datacap.sql.node.TableConstraint;
import io.edurt.datacap.sql.node.element.ColumnElement;
import io.edurt.datacap.sql.node.element.SelectElement;
import io.edurt.datacap.sql.node.element.TableElement;
import io.edurt.datacap.sql.statement.CreateDatabaseStatement;
import io.edurt.datacap.sql.statement.CreateTableStatement;
import io.edurt.datacap.sql.statement.DropDatabaseStatement;
import io.edurt.datacap.sql.statement.DropTableStatement;
import io.edurt.datacap.sql.statement.InsertStatement;
import io.edurt.datacap.sql.statement.SQLStatement;
import io.edurt.datacap.sql.statement.SelectStatement;
import io.edurt.datacap.sql.statement.UseDatabaseStatement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SQLExecutor
{
    private final DatabaseManager databaseManager;
    private TableManager tableManager;

    public SQLExecutor(DatabaseManager databaseManager)
    {
        this.databaseManager = databaseManager;
    }

    public <T> SQLResult<T> execute(String sql)
    {
        try {
            SQLStatement statement = SQLParser.parse(sql);

            if (statement instanceof CreateDatabaseStatement) {
                CreateDatabaseStatement createDatabaseStatement = (CreateDatabaseStatement) statement;
                return (SQLResult<T>) executeCreateDatabase(createDatabaseStatement);
            }

            if (statement instanceof DropDatabaseStatement) {
                DropDatabaseStatement dropDatabaseStatement = (DropDatabaseStatement) statement;
                return (SQLResult<T>) executeDropDatabase(dropDatabaseStatement);
            }

            if (statement instanceof UseDatabaseStatement) {
                UseDatabaseStatement useDatabaseStatement = (UseDatabaseStatement) statement;
                return (SQLResult<T>) executeUseDatabase(useDatabaseStatement);
            }

            if (statement instanceof CreateTableStatement) {
                ensureCurrentTableManager();
                CreateTableStatement createTableStatement = (CreateTableStatement) statement;
                return (SQLResult<T>) executeCreateTable(createTableStatement);
            }

            if (statement instanceof DropTableStatement) {
                ensureCurrentTableManager();
                DropTableStatement dropTableStatement = (DropTableStatement) statement;
                return (SQLResult<T>) executeDropTable(dropTableStatement);
            }

            if (statement instanceof InsertStatement) {
                ensureCurrentTableManager();
                InsertStatement insertStatement = (InsertStatement) statement;
                return (SQLResult<T>) executeInsert(insertStatement);
            }

            if (statement instanceof SelectStatement) {
                ensureCurrentTableManager();
                SelectStatement selectStatement = (SelectStatement) statement;
                return (SQLResult<T>) executeSelect(selectStatement);
            }

            return new SQLResult<>(false, String.format("Unsupported SQL statement: %s", statement));
        }
        catch (Exception e) {
            return new SQLResult<>(false, e.getMessage());
        }
    }

    private void ensureCurrentTableManager()
            throws DatabaseException
    {
        DatabaseDefinition currentDatabase = databaseManager.getCurrentDatabase();
        if (tableManager == null && currentDatabase != null) {
            tableManager = currentDatabase.getTableManager();
        }
    }

    private SQLResult<Void> executeCreateDatabase(CreateDatabaseStatement statement)
    {
        try {
            String databaseName = statement.getDatabaseName();

            // 检查是否带有 IF NOT EXISTS
            // Check if IF NOT EXISTS is present
            if (statement.isIfNotExists() && databaseManager.databaseExists(databaseName)) {
                return new SQLResult<>(true, "Database already exists");
            }

            // 执行创建数据库
            // Execute database creation
            databaseManager.createDatabase(databaseName);
            return new SQLResult<>(true, "Database created successfully");
        }
        catch (DatabaseException e) {
            return new SQLResult<>(false, "Failed to create database: " + e.getMessage());
        }
    }

    private SQLResult<Void> executeDropDatabase(DropDatabaseStatement statement)
    {
        try {
            if (statement.isIfNotExists() && !databaseManager.databaseExists(statement.getDatabaseName())) {
                return new SQLResult<>(true, "Database does not exist");
            }

            databaseManager.dropDatabase(statement.getDatabaseName());
            return new SQLResult<>(true, "Database dropped successfully");
        }
        catch (DatabaseException e) {
            return new SQLResult<>(false, "Failed to drop database: " + e.getMessage());
        }
    }

    private SQLResult<Void> executeUseDatabase(UseDatabaseStatement statement)
    {
        try {
            String databaseName = statement.getDatabaseName();
            databaseManager.useDatabase(databaseName);
            return new SQLResult<>(true, "Database changed");
        }
        catch (DatabaseException e) {
            return new SQLResult<>(false, "Failed to use database: " + e.getMessage());
        }
    }

    private SQLResult<Void> executeCreateTable(CreateTableStatement statement)
    {
        try {
            List<ColumnDefinition> columns = convertToColumns(statement.getColumns());

            TableDefinition metadata = new TableDefinition(statement.getTableName(), columns);
            tableManager.createTable(metadata);
            return new SQLResult<>(true, "Table created successfully");
        }
        catch (Exception e) {
            return new SQLResult<>(false, "Failed to create table: " + e.getMessage());
        }
    }

    private SQLResult<Void> executeDropTable(DropTableStatement statement)
    {
        try {
            tableManager.dropTable(statement.getTableNames().get(0));
            return new SQLResult<>(true, "Table dropped successfully");
        }
        catch (Exception e) {
            return new SQLResult<>(false, "Failed to drop table: " + e.getMessage());
        }
    }

    private SQLResult<Integer> executeInsert(InsertStatement statement)
    {
        try {
            // TODO: Support check is multiple insert for InsertStatement
            if (statement.getSimpleValues().size() == 1) {
                tableManager.insert(
                        statement.getTableName(),
                        statement.getColumns(),
                        statement.getSimpleValues().get(0)
                );
            }
            else {
                tableManager.batchInsert(
                        statement.getTableName(),
                        statement.getColumns(),
                        statement.getSimpleValues()
                );
            }

            return new SQLResult<>(true, String.format("Inserted %d rows", statement.getSimpleValues().size()));
        }
        catch (Exception e) {
            return new SQLResult<>(false, "Failed to insert rows: " + e.getMessage());
        }
    }

    private SQLResult<List<RowDefinition>> executeSelect(SelectStatement statement)
    {
        try {
            List<RowDefinition> rows = tableManager.select(
                    statement.getFromSources().get(0).getTableName(),
                    statement.getSelectElements().stream()
                            .map(SelectElement::getColumn)
                            .collect(Collectors.toList()),
                    null
            );
            return new SQLResult<>(
                    true,
                    String.format("Selected %d rows", rows.size()),
                    rows
            );
        }
        catch (Exception e) {
            return new SQLResult<>(false, "Failed to select rows: " + e.getMessage());
        }
    }

    private List<ColumnDefinition> convertToColumns(List<TableElement> elements)
    {
        List<ColumnDefinition> columns = new ArrayList<>();

        // First pass: collect all columns and primary key constraints
        Set<String> primaryKeyColumns = new HashSet<>();
        for (TableElement element : elements) {
            if (element instanceof TableConstraint) {
                TableConstraint constraint = (TableConstraint) element;
                if (constraint.getType() == ConstraintType.PRIMARY_KEY && constraint.getColumns() != null) {
                    primaryKeyColumns.addAll(Arrays.asList(constraint.getColumns()));
                }
            }
        }

        // Second pass: create column definitions
        for (TableElement element : elements) {
            if (element instanceof ColumnElement) {
                ColumnElement col = (ColumnElement) element;
                boolean isPrimaryKey = primaryKeyColumns.contains(col.getColumnName());
                boolean isNullable = true;

                // Check column constraints
                for (ColumnConstraint constraint : col.getConstraints()) {
                    if (constraint.getType() == ConstraintType.PRIMARY_KEY) {
                        isPrimaryKey = true;
                    }
                    else if (constraint.getType() == ConstraintType.NOT_NULL) {
                        isNullable = false;
                    }
                }

                DataType type = convertDataType(col.getDataType());
                columns.add(new ColumnDefinition(
                        col.getColumnName(),
                        type,
                        isPrimaryKey,
                        isNullable));
            }
        }

        return columns;
    }

    private DataType convertDataType(io.edurt.datacap.sql.node.DataType sourceType)
    {
        // Implement conversion logic from source DataType to target DataType
        // This depends on your DataType enum definition
        return DataType.valueOf(sourceType.getBaseType());
    }
}
