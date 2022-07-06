package com.wustzdy.springboot.flowable.demo.util;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.sensetime.cloud.flow.FlowConstants;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class ActUtils {

	private static final Map<String, String> types = new HashMap<>();

	static {
		types.put("userTask", "用户任务");
		types.put("serviceTask", "系统任务");
		types.put("startEvent", "开始节点");
		types.put("endEvent", "结束节点");
		types.put("exclusiveGateway", "条件判断节点");
		types.put("inclusiveGateway", "并行处理任务");
		types.put("callActivity", "子流程");
	}

	public static String getTypeCN(String type) {
		String typCN = types.get(type);
		if (typCN == null) {
			return type;
		}

		return typCN;
	}

//	public static boolean isMp(String processInstanceId) {
//		runtimeService.getVariable(processInstanceId, tagPre + variableName);
//	}

	public static boolean isMp(Map<String, Object> vars) {
		return isFieldTrue(vars, FlowConstants.MP_FLAG);
//		if (CollectionUtil.isEmpty(vars)) {
//			return false;
//		}
//
//		Object mp = vars.get(FlowConstants.MP_FLAG);
//		if (mp == null) {
//			return false;
//		}
//
//		if (mp instanceof String) {
//			if ("true".equalsIgnoreCase((String) mp)) {
//				return true;
//			}
//
//			return false;
//		}
//
//		if (mp instanceof Boolean) {
//			return (Boolean)mp;
//		}
//
//		log.warn("MP Flag is invalid:" + mp);
//
//		return false;
	}

	public static boolean isProxyOff(Map<String, Object> vars) {
		return isFieldTrue(vars, FlowConstants.PROXY_FLAG);
//		if (CollectionUtil.isEmpty(vars)) {
//			return false;
//		}
//
//		Object proxyFlag = vars.get(FlowConstants.PROXY_FLAG);
//		if (proxyFlag == null) {
//			return false;
//		}
//
//		if (proxyFlag instanceof String) {
//			if ("true".equalsIgnoreCase((String) proxyFlag)) {
//				return true;
//			}
//
//			return false;
//		}
//
//		if (proxyFlag instanceof Boolean) {
//			return (Boolean)proxyFlag;
//		}
//
//		log.warn("Proxy Flag is invalid: " + proxyFlag);
//
//		return false;
	}

	public static boolean isSilent(Map<String, Object> vars) {
		return isFieldTrue(vars, FlowConstants.SILENT_FLAG);
//		if (CollectionUtil.isEmpty(vars)) {
//			return false;
//		}
//
//		Object silentFlag = vars.get(FlowConstants.SILENT_FLAG);
//		if (silentFlag == null) {
//			return false;
//		}
//
//		if (silentFlag instanceof String) {
//			if ("true".equalsIgnoreCase((String) silentFlag)) {
//				return true;
//			}
//
//			return false;
//		}
//
//		if (silentFlag instanceof Boolean) {
//			return (Boolean)silentFlag;
//		}
//
//		log.warn("Silent Flag is invalid: " + silentFlag);
//
//		return false;
	}

	public static boolean isTmplPrior(Map<String, Object> vars) {
		return isFieldTrue(vars, FlowConstants.TMPL_PRIOR_FLAG);
//		if (CollectionUtil.isEmpty(vars)) {
//			return false;
//		}
//
//		Object tmplPriorFlag = vars.get(FlowConstants.TMPL_PRIOR_FLAG);
//		if (tmplPriorFlag == null) {
//			return false;
//		}
//
//		if (tmplPriorFlag instanceof String) {
//			if ("true".equalsIgnoreCase((String) tmplPriorFlag)) {
//				return true;
//			}
//
//			return false;
//		}
//
//		if (tmplPriorFlag instanceof Boolean) {
//			return (Boolean)tmplPriorFlag;
//		}
//
//		log.warn("TMPL Prior Flag is invalid: " + tmplPriorFlag);
//
//		return false;
	}

	public static boolean isFieldTrue(Map<String, Object> vars, String filed) {
		if (CollectionUtil.isEmpty(vars)) {
			return false;
		}

		Object fieldFlag = vars.get(filed);
		if (fieldFlag == null) {
			return false;
		}

		if (fieldFlag instanceof String) {
			if ("true".equalsIgnoreCase((String) fieldFlag)) {
				return true;
			}

			return false;
		}

		if (fieldFlag instanceof Boolean) {
			return (Boolean)fieldFlag;
		}

		log.warn( filed + " is invalid: " + fieldFlag);

		return false;
	}

	public static String getDebugLink(Map<String, Object> vars) {
		if (CollectionUtil.isEmpty(vars)) {
			log.debug("vars is empty");
			return null;
		}

		Object debugLink = vars.get(FlowConstants.DEBUG_LINK);
		if (debugLink == null || !(debugLink instanceof String)) {
			log.debug("debugLink is empty or not string");
			return null;
		}

		if (StrUtil.isBlank((String) debugLink)) {
			log.debug("debugLink is blank string");
			return null;
		}

		return (String) debugLink;
	}

}
