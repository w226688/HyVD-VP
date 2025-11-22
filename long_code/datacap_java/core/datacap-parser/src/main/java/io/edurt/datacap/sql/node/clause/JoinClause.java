package io.edurt.datacap.sql.node.clause;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.edurt.datacap.sql.node.Expression;
import io.edurt.datacap.sql.node.element.TableElement;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@SuppressFBWarnings(value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2"})
public class JoinClause
{
    private JoinType joinType;
    private TableElement rightTable;
    private Expression condition;

    public enum JoinType
    {
        INNER, LEFT, RIGHT, FULL, CROSS, NATURAL
    }
}
