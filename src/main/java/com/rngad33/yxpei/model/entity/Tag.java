package com.rngad33.yxpei.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 标签模型
 */
@Data
@Table(value = "tag")
public class Tag implements Serializable {

    /**
     * 标签 id
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 标签名称
     */
    private String tagName;

    /**
     * 父标签（分类）
     */
    private String category;

    /**
     * 父标签 id
     */
    private Long parentId;

    /**
     * 创建用户 id
     */
    private Long creatorId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否为父标签？ 0-否，1-是
     */
    private Integer isParent;

    /**
     * 是否删除？ 0-未删，1-已删
     */
    @Column(isLogicDelete = true)
    private Integer isDelete;

    @Column
    private static final long serialVersionUID = 3191241716373120793L;

}