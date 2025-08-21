package com.rngad33.yxpei.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import java.io.Serializable;

/**
 * 用户管理请求体
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserManageRequest{

    private Long id;

}