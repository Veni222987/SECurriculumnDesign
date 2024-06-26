package org.pi.server.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pi.server.service.AliyunEmailService;
import org.pi.server.utils.AliEmailUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author hu1hu
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class AliyunEmailServiceImpl implements AliyunEmailService {
    private final AliEmailUtils aliEmailUtils;

    /**
     * 发送邮件
     * @param template 模板
     * @param toAddress 收件人
     * @param map 模板参数
     * @throws Exception 异常
     */
    @Override
    public void send(String template, String toAddress, Map<String, String> map) throws Exception {
        String text = aliEmailUtils.buildContent(template, map);
        aliEmailUtils.send(text, toAddress);
    }
}
