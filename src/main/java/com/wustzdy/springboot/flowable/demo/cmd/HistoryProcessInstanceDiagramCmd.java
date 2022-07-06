package com.wustzdy.springboot.flowable.demo.cmd;



import com.wustzdy.springboot.flowable.demo.demo.CustomProcessDiagramGenerator;
import org.flowable.common.engine.impl.interceptor.Command;
import org.flowable.common.engine.impl.interceptor.CommandContext;

import java.io.InputStream;

/**
 * @author xielianjun
 */
public class HistoryProcessInstanceDiagramCmd implements Command<InputStream> {
    protected String historyProcessInstanceId;

    public HistoryProcessInstanceDiagramCmd(String historyProcessInstanceId) {
        this.historyProcessInstanceId = historyProcessInstanceId;
    }

    @Override
    public InputStream execute(CommandContext commandContext) {
        try {
            CustomProcessDiagramGenerator customProcessDiagramGenerator = new CustomProcessDiagramGenerator();

            return customProcessDiagramGenerator.generateDiagram(historyProcessInstanceId);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
