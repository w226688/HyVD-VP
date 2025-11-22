package io.edurt.datacap.sql.formatter;

import io.edurt.datacap.sql.statement.SQLStatement;
import io.edurt.datacap.sql.statement.SelectStatement;

public abstract class SQLFormatter
{
    protected static final String INDENT = "    ";

    public String format(SQLStatement statement)
    {
        if (statement instanceof SelectStatement) {
            return new SelectFormatter().format(statement);
        }
        else {
            throw new UnsupportedOperationException("Unsupported statement: " + statement);
        }
    }
}
