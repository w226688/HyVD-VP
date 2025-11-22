package io.edurt.datacap.condor.condition;

import io.edurt.datacap.condor.ComparisonOperator;
import io.edurt.datacap.condor.metadata.RowDefinition;

import java.util.Comparator;

public class SimpleCondition
        implements Condition
{
    private final String columnName;
    private final Object value;
    private final ComparisonOperator operator;

    public SimpleCondition(String columnName, Object value, ComparisonOperator operator)
    {
        this.columnName = columnName;
        this.value = value;
        this.operator = operator;
    }

    @Override
    public boolean evaluate(RowDefinition row)
    {
        Object rowValue = row.getValue(columnName);
        if (rowValue == null || value == null) {
            return false;
        }

        switch (operator) {
            case EQUALS:
                return value.equals(rowValue);
            case GREATER_THAN:
                return Comparator.comparing(Object::toString).compare(rowValue, value) > 0;
            case LESS_THAN:
                return Comparator.comparing(Object::toString).compare(rowValue, value) < 0;
//                return ((Comparable) rowValue).compareTo(value) < 0;
            default:
                return false;
        }
    }
}
