package com.wustzdy.springboot.flowable.demo.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.flowable.engine.repository.ProcessDefinition;


import java.io.Serializable;
import java.util.Date;


@Data
@TableName("ACT_RE_PROCDEF")
public class ProcdefineVo implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     *
     */
    @TableId
    private String id;
    /**
     *
     */
    private Integer rev;
    /**
     *
     */
    private String category;
    /**
     *
     */
    private String name;
    /**
     *
     */
    private String key;
    /**
     *
     */
    private Integer version;
    /**
     *
     */
    private String deploymentId;
    /**
     *
     */
    private String resourceName;
    /**
     *
     */
    private String dgrmResourceName;
    /**
     *
     */
    private String description;
    /**
     *
     */
    private Integer hasStartFormKey;
    /**
     *
     */
    private Boolean hasGraphicalNotation;
    /**
     *
     */
    private Boolean suspensionState;
    /**
     *
     */
    private String tenantId;

    @TableField(exist = false)
    private Date deploymentTime;

    public ProcdefineVo() {
    }

    public ProcdefineVo(ProcessDefinition procdefEntity) {
        this.id = procdefEntity.getId();
        this.category = procdefEntity.getCategory();
        this.name = procdefEntity.getName();
        this.key = procdefEntity.getKey();
        this.version = procdefEntity.getVersion();
        this.deploymentId = procdefEntity.getDeploymentId();
        this.resourceName = procdefEntity.getResourceName();
        this.dgrmResourceName = procdefEntity.getDiagramResourceName();
        this.description = procdefEntity.getDescription();
        this.hasGraphicalNotation = procdefEntity.hasGraphicalNotation();
        this.suspensionState = procdefEntity.isSuspended();
        this.tenantId = procdefEntity.getTenantId();
    }
}
