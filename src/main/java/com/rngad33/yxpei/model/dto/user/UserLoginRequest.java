package com.rngad33.yxpei.model.dto.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * 用户登录请求体
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserLoginRequest {

    private String userName, userPassword;

}