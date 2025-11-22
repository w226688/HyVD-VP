package io.edurt.datacap.sql.node.element;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.edurt.datacap.sql.node.ColumnConstraint;
import io.edurt.datacap.sql.node.DataType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@SuppressFBWarnings(value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2"})
public class ColumnElement
        extends TableElement
{
    private final String columnName;
    private final DataType dataType;
    private final ColumnConstraint[] constraints;

    public ColumnElement(String columnName, DataType dataType, ColumnConstraint[] constraints)
    {
        this.columnName = columnName;
        this.dataType = dataType;
        this.constraints = constraints;
    }
}