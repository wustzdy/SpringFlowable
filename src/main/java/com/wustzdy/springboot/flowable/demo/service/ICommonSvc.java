package com.wustzdy.springboot.flowable.demo.service;


import com.wustzdy.springboot.flowable.demo.util.IMap;

import java.util.List;

public interface ICommonSvc {

    String translate(String typeId, String dataId);

    String translate(String objectId, String tableName, String sourceId, String sourceValue);

    String translate(String objectId, String tableName, String sourceId1, String sourceValue1, String sourceId2, String sourceValue2);

    String getTranslate(String sql);

    List<IMap> getOptionList(String typeId, String dataId);

    List<IMap> getOptionList(String objectId, String ojbectValue, String tableName, String sourceId, String sourceValue);

    List<IMap> getOptionList(String objectId, String ojbectValue, String tableName, String sourceId, String operType, String sourceValue);

    List<IMap> getOptions(String sql);


    //动态sql
    List<IMap> executeSelectSql(String sql);

    List<String> executeSelectSql2Str(String sql);

    String executeSelectSql2SingleStr(String sql);

    void executeUpdateSql(String sql);

    void executeDeleteSql(String sql);

    int executeInsertSql(String sql);

    //序列
    String getSeq(String sequenceName);

    //数值型 序列
    Long getSequence(String sequenceName);

    String get8Seq(String sequenceName);

    String getBusinessKey(String sequenceName, String areaCode);

    String getOrderNum(String businessKey);

    Object getObject(String objectId, String tableName, String sourceId, String sourceValue);

}
