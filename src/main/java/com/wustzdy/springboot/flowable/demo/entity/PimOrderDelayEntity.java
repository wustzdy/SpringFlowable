package com.wustzdy.springboot.flowable.demo.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;


@Data
@TableName("pim_order_delay")
public class PimOrderDelayEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * ID
	 */
	@TableId
	private Integer id;
	/**
	 * 工单号
	 */
	private String orderNum;
	/**
	 * 工单大类
	 */
	private String orderTypeId;
	/**
	 * 工单小类
	 */
	private String orderTypeContentId;
	/**
	 * 0-未超时，1-超时，2-IDC类型超时
	 */
	private Integer timeStatus;
	/**
	 * 工单流程状态
	 */
	private String orderStatus;
	/**
	 * 设备数量
	 */
	private Integer idcCount;
	/**
	 * 申请时间
	 */
	private Date applyTime;
	/**
	 * 预计处理时间
	 */
	private Date expectedTime;
	/**
	 * 修改时间
	 */
	private Date updateTime;

}
