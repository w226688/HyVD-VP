package io.edurt.datacap.common.response;

import com.fasterxml.jackson.annotation.JsonView;
import io.edurt.datacap.common.enums.ServiceState;
import io.edurt.datacap.common.enums.State;
import io.edurt.datacap.common.view.EntityView;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CommonResponse<T>
{
    @JsonView(value = {EntityView.AdminView.class, EntityView.UserView.class})
    private Boolean status;

    @JsonView(value = {EntityView.AdminView.class, EntityView.UserView.class})
    private Integer code;

    @JsonView(value = {EntityView.AdminView.class, EntityView.UserView.class})
    private Object message;

    @JsonView(value = {EntityView.AdminView.class, EntityView.UserView.class})
    private T data;

    public static <T> CommonResponse<T> success(T data)
    {
        CommonResponse<T> commonResponse = new CommonResponse<>();
        commonResponse.code = State.SUCCESS.getCode();
        commonResponse.message = State.SUCCESS.getValue();
        commonResponse.data = data;
        commonResponse.status = true;
        return commonResponse;
    }

    public static <T> CommonResponse<T> failure(String message)
    {
        CommonResponse<T> commonResponse = new CommonResponse<>();
        commonResponse.code = State.FAILURE.getCode();
        commonResponse.message = message;
        commonResponse.status = false;
        return commonResponse;
    }

    public static <T> CommonResponse<T> failure(ServiceState state)
    {
        CommonResponse<T> commonResponse = new CommonResponse<>();
        commonResponse.code = state.getCode();
        commonResponse.message = state.getValue();
        commonResponse.status = false;
        return commonResponse;
    }

    public static <T> CommonResponse<T> failure(ServiceState state, Object message)
    {
        CommonResponse<T> commonResponse = new CommonResponse<>();
        commonResponse.code = state.getCode();
        commonResponse.message = message;
        commonResponse.status = false;
        return commonResponse;
    }
}
