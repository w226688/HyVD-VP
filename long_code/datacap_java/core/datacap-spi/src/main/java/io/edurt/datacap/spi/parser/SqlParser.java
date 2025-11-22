package io.edurt.datacap.spi.parser;

import io.edurt.datacap.sql.SQLParser;
import io.edurt.datacap.sql.statement.SQLStatement;

public class SqlParser
        implements Parser
{
    private final String content;

    public SqlParser(String content)
    {
        this.content = content;
    }

    @Override
    public SQLStatement getStatement()
    {
        return SQLParser.parse(content.trim());
    }

    @Override
    public String getExecuteContext()
    {
        return null;
    }
}
