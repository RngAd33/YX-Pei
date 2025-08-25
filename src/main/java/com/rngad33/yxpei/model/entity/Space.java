package com.rngad33.yxpei.model.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

/**
 * 空间模型
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Space {

    /**
     * 空间id
     */
    private Long id;

    /**
     * 空间成员表
     */
    private List<User> users;

}