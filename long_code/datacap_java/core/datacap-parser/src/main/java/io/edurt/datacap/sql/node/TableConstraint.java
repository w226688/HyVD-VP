package io.edurt.datacap.sql.node;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.edurt.datacap.sql.node.clause.ForeignKeyClause;
import io.edurt.datacap.sql.node.element.TableElement;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@SuppressFBWarnings(value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2"})
public class TableConstraint
        extends TableElement
{
    private final String constraintName;
    private final ConstraintType type;
    private final String[] columns;
    private final ForeignKeyClause foreignKey;
    private final Expression checkExpression;

    public TableConstraint(String constraintName, ConstraintType type, String[] columns,
            ForeignKeyClause foreignKey, Expression checkExpression)
    {
        this.constraintName = constraintName;
        this.type = type;
        this.columns = columns;
        this.foreignKey = foreignKey;
        this.checkExpression = checkExpression;
    }
}
