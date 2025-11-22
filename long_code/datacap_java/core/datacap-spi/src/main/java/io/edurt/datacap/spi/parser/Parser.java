package io.edurt.datacap.spi.parser;

import io.edurt.datacap.sql.statement.SQLStatement;

public interface Parser
{
    SQLStatement getStatement();

    String getExecuteContext();
}
