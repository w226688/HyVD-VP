package io.edurt.datacap.sql.node.element;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.edurt.datacap.sql.node.clause.JoinClause;
import io.edurt.datacap.sql.statement.SelectStatement;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@SuppressFBWarnings(value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2"})
public class TableElement
{
    private String tableName;
    private String alias;
    private List<JoinClause> joins;
    private SelectStatement subquery;
}
