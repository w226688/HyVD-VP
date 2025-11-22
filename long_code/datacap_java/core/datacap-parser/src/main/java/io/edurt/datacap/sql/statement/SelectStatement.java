package io.edurt.datacap.sql.statement;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.edurt.datacap.sql.node.Expression;
import io.edurt.datacap.sql.node.clause.LimitClause;
import io.edurt.datacap.sql.node.element.OrderByElement;
import io.edurt.datacap.sql.node.element.SelectElement;
import io.edurt.datacap.sql.node.element.TableElement;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
@SuppressFBWarnings(value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2"})
public class SelectStatement
        extends SQLStatement
{
    private List<SelectElement> selectElements;
    private List<TableElement> fromSources;
    private Expression whereClause;
    private List<Expression> groupByElements;
    private Expression havingClause;
    private List<OrderByElement> orderByElements;
    private LimitClause limitClause;

    public SelectStatement()
    {
        super(StatementType.SELECT);
    }
}
