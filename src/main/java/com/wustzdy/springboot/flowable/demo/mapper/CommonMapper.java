package com.wustzdy.springboot.flowable.demo.mapper;

import com.wustzdy.springboot.flowable.demo.util.IMap;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author xielianjun
 */
@Mapper
public interface CommonMapper {

    List<IMap> executeSelectSql2Map(IMap params);

    List<String> executeSelectSql2Str(IMap params);

    String executeSelectSql2SingleStr(IMap params);

    void executeUpdateSql(IMap params);

    void executeDeleteSql(IMap params);

    int executeInsertSql(IMap params);

    String getSeq(IMap params);

    Long getSequence(String seq);

    String get4Seq(IMap params);

    String get6Seq(IMap params);

    String get8Seq(IMap params);

    String getYmd4Seq(IMap params);

    String getYmd();

}