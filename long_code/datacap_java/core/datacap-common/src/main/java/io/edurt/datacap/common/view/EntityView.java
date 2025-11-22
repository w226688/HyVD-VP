package io.edurt.datacap.common.view;

public class EntityView
{
    public interface NoneView {}

    public interface BasicView {}

    public interface UserView
            extends BasicView {}

    public interface AdminView
            extends BasicView {}
}
