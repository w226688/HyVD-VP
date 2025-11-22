package io.edurt.datacap.sql.node.element;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.edurt.datacap.sql.node.Expression;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@SuppressFBWarnings(value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2"})
public class SelectElement
{
    private String column;
    private String alias;
    private Expression expression;
}
