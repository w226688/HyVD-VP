package io.edurt.datacap.condor.manager;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.edurt.datacap.condor.DataType;
import io.edurt.datacap.condor.TableException;
import io.edurt.datacap.condor.condition.Condition;
import io.edurt.datacap.condor.io.AppendableObjectInputStream;
import io.edurt.datacap.condor.io.AppendableObjectOutputStream;
import io.edurt.datacap.condor.metadata.ColumnDefinition;
import io.edurt.datacap.condor.metadata.RowDefinition;
import io.edurt.datacap.condor.metadata.TableDefinition;
import lombok.extern.slf4j.Slf4j;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Stream;

@Slf4j
@SuppressFBWarnings(value = {"DLS_DEAD_LOCAL_STORE", "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE"})
public class TableManager
{
    private final Path dataDir;
    private final Map<String, TableDefinition> tableMetadataCache;
    private final Map<String, ReadWriteLock> tableLocks;

    public TableManager(Path databasePath)
    {
        this.dataDir = databasePath.resolve("tables");
        this.tableMetadataCache = new HashMap<>();
        this.tableLocks = new HashMap<>();
        initializeDirectory();
    }

    private void initializeDirectory()
    {
        loadExistingTables();
    }

    private void loadExistingTables()
    {
        if (!Files.exists(dataDir)) {
            return;
        }

        log.info("Loading existing tables from {}", dataDir);
        try (Stream<Path> stream = Files.walk(dataDir, 1)) {
            stream.filter(Files::isDirectory)
                    .filter(path -> !path.equals(dataDir))
                    .filter(path -> Files.exists(path.resolve("metadata/table.meta")))
                    .forEach(tableDir -> {
                        String tableName = tableDir.getFileName().toString();
                        try {
                            TableDefinition metadata = loadTableMetadata(tableName);
                            tableMetadataCache.put(tableName, metadata);
                            tableLocks.put(tableName, new ReentrantReadWriteLock());
                        }
                        catch (IOException e) {
                            log.error("Failed to load table metadata: {}", tableName);
                        }
                    });
        }
        catch (IOException e) {
            log.error("Failed to load existing tables", e);
        }
    }

    public void createTable(TableDefinition metadata)
            throws TableException
    {
        validateTableName(metadata.getTableName());

        if (tableExists(metadata.getTableName())) {
            throw new TableException("Table '" + metadata.getTableName() + "' already exists");
        }

        try {
            saveTableMetadata(metadata);

            createTableDataFile(metadata.getTableName());

            tableMetadataCache.put(metadata.getTableName(), metadata);
            tableLocks.put(metadata.getTableName(), new ReentrantReadWriteLock());
        }
        catch (IOException e) {
            throw new TableException("Failed to create table: " + e.getMessage());
        }
    }

    public void dropTable(String tableName)
            throws TableException
    {
        if (!tableExists(tableName)) {
            throw new TableException("Table '" + tableName + "' does not exist");
        }

        ReadWriteLock lock = tableLocks.get(tableName);
        lock.writeLock().lock();
        try {
            deleteFile(dataDir.resolve(tableName));
            tableMetadataCache.remove(tableName);
            tableLocks.remove(tableName);
        }
        catch (IOException e) {
            throw new TableException("Failed to drop table: " + e.getMessage());
        }
        finally {
            lock.writeLock().unlock();
        }
    }

    public void insert(String tableName, List<String> columnNames, List<Object> values)
            throws TableException
    {
        TableDefinition metadata = getTableMetadata(tableName);
        ReadWriteLock lock = tableLocks.get(tableName);

        lock.writeLock().lock();
        try {
            validateInsertData(metadata, columnNames, values);
            RowDefinition row = createRow(metadata, columnNames, values);
            appendRowToFile(tableName, row);
        }
        finally {
            lock.writeLock().unlock();
        }
    }

    public void batchInsert(String tableName, List<String> columnNames, List<List<Object>> valuesList)
            throws TableException
    {
        TableDefinition metadata = getTableMetadata(tableName);
        ReadWriteLock lock = tableLocks.get(tableName);

        lock.writeLock().lock();
        try {
            // Validate all rows first
            for (List<Object> values : valuesList) {
                validateInsertData(metadata, columnNames, values);
            }

            // Create all rows
            List<RowDefinition> rows = new ArrayList<>();
            for (List<Object> values : valuesList) {
                rows.add(createRow(metadata, columnNames, values));
            }

            // Batch write to file
            Path dataPath = dataDir.resolve(tableName)
                    .resolve("data")
                    .resolve("table.data");
            try (ObjectOutputStream oos = new AppendableObjectOutputStream(
                    Files.newOutputStream(dataPath, StandardOpenOption.APPEND))) {
                for (RowDefinition row : rows) {
                    oos.writeObject(row);
                }
            }
            catch (IOException e) {
                throw new TableException("Failed to batch insert rows: " + e.getMessage());
            }
        }
        finally {
            lock.writeLock().unlock();
        }
    }

    public int update(String tableName, Map<String, Object> setValues, Condition whereCondition)
            throws TableException
    {
        TableDefinition metadata = getTableMetadata(tableName);
        ReadWriteLock lock = tableLocks.get(tableName);

        lock.writeLock().lock();
        try {
            List<RowDefinition> rows = readAllRows(tableName);
            int updatedCount = 0;

            for (RowDefinition row : rows) {
                if (whereCondition == null || whereCondition.evaluate(row)) {
//                    updateRow(row, setValues);
                    updatedCount++;
                }
            }

            if (updatedCount > 0) {
                saveAllRows(tableName, rows);
            }

            return updatedCount;
        }
        finally {
            lock.writeLock().unlock();
        }
    }

    public int delete(String tableName, Condition whereCondition)
            throws TableException
    {
        ReadWriteLock lock = tableLocks.get(tableName);

        lock.writeLock().lock();
        try {
            List<RowDefinition> rows = readAllRows(tableName);
            List<RowDefinition> remainingRows = new ArrayList<>();
            int deletedCount = 0;

            for (RowDefinition row : rows) {
                if (whereCondition == null || !whereCondition.evaluate(row)) {
                    remainingRows.add(row);
                }
                else {
                    deletedCount++;
                }
            }

            if (deletedCount > 0) {
                saveAllRows(tableName, remainingRows);
            }

            return deletedCount;
        }
        finally {
            lock.writeLock().unlock();
        }
    }

    public List<RowDefinition> select(String tableName, List<String> columnNames, Condition whereCondition)
            throws TableException
    {
        TableDefinition metadata = getTableMetadata(tableName);
        ReadWriteLock lock = tableLocks.get(tableName);

        if (columnNames != null && !columnNames.isEmpty()) {
            for (String columnName : columnNames) {
                if (metadata.getColumn(columnName) == null) {
                    throw new TableException("Column '" + columnName + "' does not exist");
                }
            }
        }

        lock.readLock().lock();
        try {
            List<RowDefinition> rows = readAllRows(tableName);
            List<RowDefinition> result = new ArrayList<>();

            for (RowDefinition row : rows) {
                if (whereCondition == null || whereCondition.evaluate(row)) {
                    if (columnNames != null && !columnNames.isEmpty()) {
                        RowDefinition projectedRow = projectRow(row, columnNames);
                        result.add(projectedRow);
                    }
                    else {
                        result.add(row);
                    }
                }
            }

            return result;
        }
        finally {
            lock.readLock().unlock();
        }
    }

    private void appendRowToFile(String tableName, RowDefinition row)
            throws TableException
    {
        Path dataPath = dataDir.resolve(tableName)
                .resolve("data")
                .resolve("table.data");
        try (ObjectOutputStream oos = new AppendableObjectOutputStream(Files.newOutputStream(dataPath, StandardOpenOption.APPEND))) {
            oos.writeObject(row);
        }
        catch (IOException e) {
            log.error("Failed to append row to file", e);
            throw new TableException("Failed to append row to file: " + e.getMessage());
        }
    }

    private void saveAllRows(String tableName, List<RowDefinition> rows)
            throws TableException
    {
        Path dataPath = Paths.get(dataDir + tableName + ".data");
        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(dataPath, StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING))) {
            for (RowDefinition row : rows) {
                oos.writeObject(row);
            }
        }
        catch (IOException e) {
            throw new TableException("Failed to save rows to file: " + e.getMessage());
        }
    }

    private List<RowDefinition> readAllRows(String tableName)
            throws TableException
    {
        List<RowDefinition> rows = new ArrayList<>();
        Path dataPath = dataDir.resolve(tableName).resolve("data").resolve("table.data");

        if (!Files.exists(dataPath)) {
            return rows;
        }

        try (AppendableObjectInputStream ois = new AppendableObjectInputStream(Files.newInputStream(dataPath))) {
            while (true) {
                try {
                    RowDefinition row = (RowDefinition) ois.readObject();
                    rows.add(row);
                }
                catch (EOFException e) {
                    break;
                }
            }
        }
        catch (IOException | ClassNotFoundException e) {
            log.error("Failed to read rows", e);
            throw new TableException("Failed to read rows: " + e.getMessage());
        }

        return rows;
    }

    private void validateTableName(String tableName)
            throws TableException
    {
        if (tableName == null || tableName.trim().isEmpty()) {
            throw new TableException("Table name cannot be empty");
        }
        if (!tableName.matches("^[a-zA-Z][a-zA-Z0-9_]*$")) {
            throw new TableException("Invalid table name");
        }
        if (tableName.length() > 64) {
            throw new TableException("Table name is too long");
        }
    }

    private void validateInsertData(TableDefinition metadata, List<String> columnNames, List<Object> values)
            throws TableException
    {
        if (columnNames.size() != values.size()) {
            throw new TableException("Column count doesn't match value count");
        }

        for (int i = 0; i < columnNames.size(); i++) {
            String columnName = columnNames.get(i);
            Object value = values.get(i);
            ColumnDefinition column = metadata.getColumn(columnName);

            if (column == null) {
                throw new TableException("Column '" + columnName + "' does not exist");
            }

            if (!isValueTypeValid(value, column.getType())) {
                throw new TableException("Invalid data type for column '" + columnName + "'");
            }
        }
    }

    private boolean isValueTypeValid(Object value, DataType expectedType)
    {
        if (value == null) {
            return true;
        }

        switch (expectedType) {
            case INTEGER:
                return value instanceof Integer;
            case VARCHAR:
                return value instanceof String;
            case BOOLEAN:
                return value instanceof Boolean;
            case DOUBLE:
                return value instanceof Double;
            default:
                return false;
        }
    }

    private RowDefinition createRow(TableDefinition metadata, List<String> columnNames, List<Object> values)
    {
        RowDefinition row = new RowDefinition();
        for (int i = 0; i < columnNames.size(); i++) {
            row.setValue(columnNames.get(i), values.get(i));
        }
        return row;
    }

    private RowDefinition projectRow(RowDefinition originalRow, List<String> columnNames)
    {
        RowDefinition projectedRow = new RowDefinition();
        for (String columnName : columnNames) {
            projectedRow.setValue(columnName, originalRow.getValue(columnName));
        }
        return projectedRow;
    }

    private void saveTableMetadata(TableDefinition metadata)
            throws IOException
    {
        Path metaPath = dataDir.resolve(metadata.getTableName())
                .resolve("metadata")
                .resolve("table.meta");
        if (!Files.exists(metaPath)) {
            Files.createDirectories(metaPath.getParent());
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(metaPath))) {
            oos.writeObject(metadata);
        }
        catch (IOException e) {
            log.error("Failed to save table metadata", e);
            throw new IOException("Failed to save table metadata", e);
        }
    }

    private void createTableDataFile(String tableName)
            throws IOException
    {
        Path metaPath = dataDir.resolve(tableName)
                .resolve("data")
                .resolve("table.data");
        if (!Files.exists(metaPath)) {
            Files.createDirectories(metaPath.getParent());
            Files.createFile(metaPath);
        }
    }

    private TableDefinition loadTableMetadata(String tableName)
            throws IOException
    {
        Path metaPath = dataDir.resolve(tableName)
                .resolve("metadata")
                .resolve("table.meta");
        try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(metaPath))) {
            return (TableDefinition) ois.readObject();
        }
        catch (IOException | ClassNotFoundException e) {
            log.error("Failed to load table metadata", e);
            throw new IOException("Failed to load table metadata", e);
        }
    }

    public boolean tableExists(String tableName)
    {
        return tableMetadataCache.containsKey(tableName);
    }

    public TableDefinition getTableMetadata(String tableName)
            throws TableException
    {
        TableDefinition metadata = tableMetadataCache.get(tableName);
        if (metadata == null) {
            throw new TableException("Table '" + tableName + "' does not exist");
        }
        return metadata;
    }

    private void deleteFile(Path tableDir)
            throws IOException
    {
        try (Stream<Path> stream = Files.walk(tableDir)) {
            stream.sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        }
                        catch (IOException e) {
                            log.error("Failed to delete: {}", path, e);
                        }
                    });
        }
    }
}
