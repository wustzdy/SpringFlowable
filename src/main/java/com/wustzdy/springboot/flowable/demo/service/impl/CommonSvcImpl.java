package com.wustzdy.springboot.flowable.demo.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.wustzdy.springboot.flowable.demo.mapper.CommonMapper;
import com.wustzdy.springboot.flowable.demo.service.ICommonSvc;
import com.wustzdy.springboot.flowable.demo.util.BaseUtils;
import com.wustzdy.springboot.flowable.demo.util.IMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Service("ICommonSvc")
public class CommonSvcImpl implements ICommonSvc {

    @Autowired
    private CommonMapper commonMapper;


    @Override
    public String translate(String typeId, String dataId) {
        IMap map = new IMap();
        String trans = "";
        if (typeId != null && !"".equals(typeId) && dataId != null && !"".equals(dataId)) {
            String sql = "SELECT * FROM PIM_TABLE_DICT T WHERE T.USE_TAG = '1' AND T.TYPE_ID = '" + typeId + "' AND T.DATA_ID = '" + dataId + "' ORDER BY ORDER_LIST";
            map.put("sql", sql);
            List<?> list = commonMapper.executeSelectSql2Map(map);
            if (list != null && list.size() > 0) {
                trans = ((IMap) list.get(0)).getString("DATA_NAME", "");
            } else {
                trans = dataId;
            }
        }
        return trans;
    }

    @Override
    public String translate(String objectId, String tableName, String sourceId,
                            String sourceValue) {
        IMap map = new IMap();
        String trans = "";
        if (objectId != null && !"".equals(objectId) && tableName != null && !"".equals(tableName) && sourceId != null &&
                !"".equals(sourceId) && sourceValue != null && !"".equals(sourceValue)) {
            String sql = "SELECT " + objectId + " FROM " + tableName + " WHERE " + sourceId + " = '" + sourceValue + "'";
            map.put("sql", sql);
            List<?> list = commonMapper.executeSelectSql2Map(map);
            if (list != null && list.size() > 0) {
                IMap map2 = new IMap();
                map2 = (IMap) list.get(0);
                if (CollectionUtil.isNotEmpty(map2)) {
                    trans = map2.getString(objectId.toUpperCase(), "");
                } else {
                    trans = sourceValue;
                }
//				trans = ((IMap) list.get(0)).getString(objectId.toUpperCase(), "");
            } else {
                trans = sourceValue;
            }
        }
        return trans;
    }

    @Override
    public String translate(String objectId, String tableName, String sourceId1, String sourceValue1, String sourceId2, String sourceValue2) {
        IMap map = new IMap();
        String trans = "";
        if (!BaseUtils.isEmpty(objectId) && !BaseUtils.isEmpty(tableName) && !BaseUtils.isEmpty(sourceId1) && !BaseUtils.isEmpty(sourceValue1)
                && !BaseUtils.isEmpty(sourceId2) && !BaseUtils.isEmpty(sourceValue2)) {
            String sql = "SELECT " + objectId + " FROM " + tableName + " WHERE " + sourceId1 + " = '" + sourceValue1 + "' AND " + sourceId2 + "='" + sourceValue2 + "'";
            map.put("sql", sql);
            List<?> list = commonMapper.executeSelectSql2Map(map);
            if (list != null && list.size() > 0) {
                trans = ((IMap) list.get(0)).getString(objectId.toUpperCase(), "");
            } else {
//				trans = sourceValue2;
            }
        }
        return trans;
    }

    @Override
    public String getTranslate(String sql) {
        IMap map = new IMap();
        String trans = "";
        if (sql != null && !"".equals(sql)) {
            map.put("sql", sql);
            List<?> list = commonMapper.executeSelectSql2Str(map);
            if (list != null && list.size() > 0) {
                trans = ((String) list.get(0));
            } else {
                trans = "";
            }
        }
        return trans;
    }

    @Override
    public List<IMap> getOptionList(String typeId, String dataId) {
        IMap map = new IMap();
        List<IMap> list = new ArrayList<IMap>();
        if (typeId != null && !"".equals(typeId) && dataId != null && !"".equals(dataId)) {
            String sql = "SELECT * FROM PIM_TABLE_DICT T WHERE T.USE_TAG = '1' AND T.TYPE_ID = '" + typeId + "' ORDER BY ORDER_LIST";
            map.put("sql", sql);
            list = commonMapper.executeSelectSql2Map(map);
        }
        return list;
    }


    @Override
    public List<IMap> getOptionList(String objectId, String ojbectValue,
                                    String tableName, String sourceId, String sourceValue) {
        IMap map = new IMap();
        List<IMap> list = new ArrayList<IMap>();
        if (objectId != null && !"".equals(objectId) && ojbectValue != null && !"".equals(ojbectValue) &&
                tableName != null && !"".equals(tableName)) {
            String sql = "SELECT " + objectId + ", " + ojbectValue + " FROM " + tableName + " WHERE " + sourceId + " = '" + sourceValue + "'";
            map.put("sql", sql);
            list = commonMapper.executeSelectSql2Map(map);
        }
        return list;
    }

    @Override
    public List<IMap> getOptionList(String objectId, String ojbectValue,
                                    String tableName, String sourceId, String operType,
                                    String sourceValue) {
        IMap map = new IMap();
        List<IMap> list = new ArrayList<IMap>();
        if (objectId != null && !"".equals(objectId) && ojbectValue != null && !"".equals(ojbectValue) &&
                tableName != null && !"".equals(tableName)) {
            String sql = "SELECT " + objectId + ", " + ojbectValue + " FROM " + tableName + " WHERE " + sourceId + " " + operType + " '" + sourceValue + "'";
            map.put("sql", sql);
            list = commonMapper.executeSelectSql2Map(map);
        }
        return list;
    }

    @Override
    public List<IMap> getOptions(String sql) {
        IMap map = new IMap();
        List<IMap> list = new ArrayList<IMap>();
        if (sql != null && !"".equals(sql)) {
            map.put("sql", sql);
            list = commonMapper.executeSelectSql2Map(map);
        }
        return list;
    }

    @Override
    public List<IMap> executeSelectSql(String sql) {
        IMap map = new IMap();
        map.put("sql", sql);
        return commonMapper.executeSelectSql2Map(map);
    }

    @Override
    public List<String> executeSelectSql2Str(String sql) {
        IMap map = new IMap();
        map.put("sql", sql);
        return commonMapper.executeSelectSql2Str(map);
    }

    @Override
    public String executeSelectSql2SingleStr(String sql) {
        IMap map = new IMap();
        map.put("sql", sql);
        return commonMapper.executeSelectSql2SingleStr(map);
    }

    @Override
    public void executeUpdateSql(String sql) {
        IMap map = new IMap();
        map.put("sql", sql);
        commonMapper.executeUpdateSql(map);
    }

    @Override
    public void executeDeleteSql(String sql) {
        IMap map = new IMap();
        map.put("sql", sql);
        commonMapper.executeDeleteSql(map);
    }

    @Override
    public int executeInsertSql(String sql) {
        IMap map = new IMap();
        map.put("sql", sql);
        int row = commonMapper.executeInsertSql(map);
        return row;
    }


    @Override
    public String getSeq(String seqId) {
        IMap map = new IMap();
        map.put("seqId", seqId);
        return commonMapper.getSeq(map);
    }

    /**
     * 得到sequence 数值型
     */
    @Override
    public Long getSequence(String seqId) {
        return commonMapper.getSequence(seqId);
    }

    @Override
    public String get8Seq(String seqId) {
        IMap map = new IMap();
        map.put("seqId", seqId);
        return commonMapper.get8Seq(map);
    }

    @Override
    public String getBusinessKey(String sequenceName, String areaCode) {
        IMap map = new IMap();
        map.put("seqId", sequenceName);
        String ymd4Seq = commonMapper.getYmd4Seq(map);

        String areaStr;
        if ("JLCU".equals(areaCode)) {
            areaStr = "JLCU";
        } else if ("0431".equals(areaCode)) {
            areaStr = "JLCC";
        } else if ("0432".equals(areaCode)) {
            areaStr = "JLJL";
        } else if ("0433".equals(areaCode)) {
            areaStr = "JLYB";
        } else if ("0434".equals(areaCode)) {
            areaStr = "JLSP";
        } else if ("0435".equals(areaCode)) {
            areaStr = "JLTH";
        } else if ("0436".equals(areaCode)) {
            areaStr = "JLBC";
        } else if ("0437".equals(areaCode)) {
            areaStr = "JLLY";
        } else if ("0438".equals(areaCode)) {
            areaStr = "JLSY";
        } else if ("0439".equals(areaCode)) {
            areaStr = "JLBS";
        } else {
            areaStr = "XXXX";
        }

        return areaStr + ymd4Seq;
    }

    @Override
    public String getOrderNum(String businessKey) {
        IMap map = new IMap();
        map.put("seqId", "SEQ_ORDER_NUM");
        String seq = commonMapper.get8Seq(map);
        return businessKey + "." + seq;
    }

    @Override
    public Object getObject(String objectId, String tableName, String sourceId,
                            String sourceValue) {
        IMap map = new IMap();
        Object trans = "";
        if (objectId != null && !"".equals(objectId) && tableName != null && !"".equals(tableName) && sourceId != null &&
                !"".equals(sourceId) && sourceValue != null && !"".equals(sourceValue)) {
            String sql = "SELECT " + objectId + " FROM " + tableName + " WHERE " + sourceId + " = '" + sourceValue + "'";
            map.put("sql", sql);
            List<?> list = commonMapper.executeSelectSql2Map(map);
            if (list != null && list.size() > 0) {
                trans = ((Map<String, Object>) list.get(0)).get(objectId.toUpperCase());
            } else {
                trans = sourceValue;
            }
        }
        return trans;
    }

}
