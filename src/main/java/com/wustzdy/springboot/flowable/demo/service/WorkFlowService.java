package com.wustzdy.springboot.flowable.demo.service;




import com.wustzdy.springboot.flowable.demo.entity.PimOrderEntity;
import com.wustzdy.springboot.flowable.demo.util.R;
import com.wustzdy.springboot.flowable.demo.vo.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public interface WorkFlowService {

    void passProcess(PassFlowVo passflowVo, String userName, String operation);

    void batchPass(Integer id, String userName);

    void rejectStopProcess(RejectFlowVo rejectFlowVo, String userName);

    void transferProcess(TransferFlowVo transferFlowVo, String userName);

    void batchDeleteProcessInstanceIdsProcess(String processInstanceId, String userName);

    void comment(CommentParamVo commentParamVo, String flowStatProcess);

    void stopProcess(String userName, String processInstanceId, String comment);

    void closeProcess(String userName, String processInstanceId, String comment);

    void urgeOrder(String userName, String processInstanceId);

    R reopen(String processInstanceId);

    String getApproveLeader(ApproveUserVo params, String username, PimOrderEntity pimOrderEntity);

    void addCandidateProcess(CandidateProInstIdParamVo candidateProInstIdParamVo);

    void comment(CommentParamVo commentParamVo);

    String getApproveLeader(ApproveUserVo params, String userName);

    byte[] responseDiagramByteByBusinessKey(String businessKey) throws IOException;

    void responseDiagramByBusinessKey(HttpServletResponse response, String businessKey) throws IOException;

    byte[] responseDiagramByteByProcessInstanceId(String processInstanceId) throws IOException;

    byte[] responseDiagramByte(String processInstanceId) throws IOException;

    void responseDiagramByProcessInstanceId(HttpServletResponse response, String processInstanceId) throws IOException;

    void responseDiagram(HttpServletResponse response, String processInstanceId) throws IOException;

}
