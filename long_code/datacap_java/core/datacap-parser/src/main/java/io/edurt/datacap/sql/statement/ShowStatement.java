package io.edurt.datacap.sql.statement;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.edurt.datacap.sql.node.Expression;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@SuppressFBWarnings(value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2"})
public class ShowStatement
        extends SQLStatement
{
    private ShowType showType;
    private String databaseName;
    private String tableName;
    private String pattern;
    private Expression whereCondition;

    public ShowStatement()
    {
        super(StatementType.SHOW);
    }

    public enum ShowType
    {
        DATABASES,
        TABLES,
        COLUMNS
    }
}
