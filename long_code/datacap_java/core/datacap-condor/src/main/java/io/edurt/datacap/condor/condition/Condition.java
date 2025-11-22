package io.edurt.datacap.condor.condition;

import io.edurt.datacap.condor.metadata.RowDefinition;

public interface Condition
{
    boolean evaluate(RowDefinition row);
}
