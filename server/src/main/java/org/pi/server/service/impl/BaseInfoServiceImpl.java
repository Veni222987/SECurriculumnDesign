package org.pi.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.pi.server.mapper.HostMapper;
import org.pi.server.mapper.TeamUserMapper;
import org.pi.server.model.entity.Host;
import org.pi.server.model.entity.TeamUser;
import org.pi.server.model.enums.HostStatusEnum;
import org.pi.server.service.BaseInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class BaseInfoServiceImpl extends ServiceImpl<HostMapper, Host> implements BaseInfoService {

    @Autowired
    private TeamUserMapper teamUserMapper;
    @Override
    public long postComputerInfo(Host host) {
        if (host.getMac() == null) {
            return 0;
        } else {
            QueryWrapper<Host> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("mac", host.getMac());
            Host hostTemp = getOne(queryWrapper);
            if (hostTemp == null) { // 新增
                host.setStatus(HostStatusEnum.UNMONITORED);
                host.setLastTime(LocalDateTime.now());
                host.setHostname(host.getMac());
                return save(host) ? host.getId() : -1;
            } else { // 更新
                if (hostTemp.getStatus() == HostStatusEnum.UNKNOWN) {
                    hostTemp.setStatus(HostStatusEnum.MONITORING);
                }
                hostTemp.setLastTime(LocalDateTime.now());
                return updateById(hostTemp) ? hostTemp.getId() : -1;
            }
        }
    }

    @Override
    public boolean putComputerInfo(String userID, String agentID, String hostname) {
        Host host = getById(agentID);
        Integer teamId = host.getTeamId();
        QueryWrapper<TeamUser> queryWrapper = new QueryWrapper();
        queryWrapper.eq("team_id", teamId).eq("user_id", userID);
        boolean exists = teamUserMapper.exists(queryWrapper);
        if (!exists) { // 不存在
            return false;
        }
        UpdateWrapper<Host> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", agentID).set("hostname",hostname);
        return update(updateWrapper);
    }

    @Override
    public Host getComputerInfo(String userID, String agentID) {
        Host host = getById(agentID);
        Integer teamId = host.getTeamId();
        QueryWrapper<TeamUser> queryWrapper = new QueryWrapper();
        queryWrapper.eq("team_id", teamId).eq("user_id", Long.parseLong(userID));
        boolean exists = teamUserMapper.exists(queryWrapper);
        if (exists) {
            return host;
        } else {
            return null;
        }
    }
}
