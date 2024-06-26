package org.pi.server.service;

import java.util.List;
import java.util.Map;

/**
 * @author hu1hu
 */
public interface InformationService {
    List<Map<String, Object>> getPerformance(String userID, String agentID, Long startTime, Long endTime);
    List<Map<String, Object>> getMetric(String userID, String agentID, Long startTime, Long endTime);

    void updateTime(String agentID);

}
