package org.pi.server.controller;

import com.xkcoding.justauth.AuthRequestFactory;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.zhyd.oauth.exception.AuthException;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.request.AuthRequest;
import me.zhyd.oauth.utils.AuthStateUtils;
import org.jetbrains.annotations.NotNull;
import org.pi.server.annotation.GetAttribute;
import org.pi.server.common.Result;
import org.pi.server.common.ResultCode;
import org.pi.server.common.ResultUtils;
import org.pi.server.model.entity.Auth;
import org.pi.server.model.entity.User;
import org.pi.server.service.AuthService;
import org.pi.server.service.UserService;
import org.pi.server.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author hu1hu
 */
@Slf4j
@RestController
@RequestMapping("/v1/oauth")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@ComponentScan({"com.xkcoding.justauth"}) // Add this line
public class AuthController {
    private final AuthRequestFactory factory;
    private final AuthService authService;
    private final UserService userService;

    /**
     * 获取支持第三方登录的类型 (本应用支持列表)
     * @return 支持的第三方登录类型
     */
    @GetMapping("/list")
    public Result<Object> list() {
        return ResultUtils.success(Map.of("list", factory.oauthList()));
    }

    /**
     * 第三方登录
     * @param type 第三方账号类型
     * @param response HttpServletResponse
     * @return ResultCode.SUCCESS 成功 ResultCode.PARAMS_ERROR 参数错误 ResultCode.SYSTEM_ERROR 系统内部异常
     */
    @GetMapping("/login/{type}")
    public Result<Object> login(@PathVariable String type, HttpServletResponse response) {
        // 参数检查
        if (type == null || type.isEmpty()) {
            return ResultUtils.error(ResultCode.PARAMS_ERROR);
        }
        if (!factory.oauthList().contains(type.toUpperCase())) {
            return ResultUtils.error(ResultCode.TYPE_NOT_SUPPORT);
        }
        // 获取第三方登录请求
        AuthRequest authRequest = factory.get(type);
        // 随机生成state，用于校验回调state
        return ResultUtils.success(Map.of("redirectURL", authRequest.authorize(AuthStateUtils.createState())));
    }

    /**
     * 绑定第三方账号
     * @param userID 用户ID
     * @param type 第三方账号类型
     * @param response HttpServletResponse
     * @throws IOException IO异常
     */
    @GetMapping("/bind/{type}")
    public Result<Object> bind(@GetAttribute("userID") @NotNull String userID, @PathVariable @NotNull String type, @NotNull HttpServletResponse response) throws IOException {
        // 参数检查
        if (!factory.oauthList().contains(type.toUpperCase())) {
            return ResultUtils.error(ResultCode.TYPE_NOT_SUPPORT);
        }
        // 获取第三方登录请求
        AuthRequest authRequest = factory.get(type);
        // 随机生成state，用于校验回调state
        String state = AuthStateUtils.createState();
        // 拼接userID用于绑定
        state = userID + ":" + state;
        return ResultUtils.success(Map.of("redirectURL", authRequest.authorize(state)));
    }

    /**
     * 解绑第三方账号
     * @param userID 用户ID
     * @param type 第三方账号类型
     * @return ResultCode.SUCCESS 成功 ResultCode.PARAMS_ERROR 参数错误
     */
    @DeleteMapping("/unbind/{type}")
    public Result<Object> unbind(@GetAttribute @NotNull String userID, @PathVariable @NotNull String type) {
        // 参数检查
        if (!factory.oauthList().contains(type.toUpperCase())) {
            return ResultUtils.error(ResultCode.TYPE_NOT_SUPPORT);
        }
        boolean unbind = authService.unbind(userID, type);
        return unbind ? ResultUtils.success() : ResultUtils.error(ResultCode.PARAMS_ERROR);
    }

    /**
     * 登录注册绑定回调函数
     * @param type 第三方账号类型
     * @param callback 回调信息
     * @return ResultCode.SUCCESS 成功 ResultCode.PARAMS_ERROR 参数错误 ResultCode.REPEAT_OPERATION 重复操作
     */
    @RequestMapping("/{type}/callback")
    public Result<Object> callback(@PathVariable @NotNull String type,AuthCallback callback) {
        long id = authService.login(type, callback);
        User user = userService.getByID(id);
        List<Auth> auths = authService.getAuthsByUserID(id);
        if (id == -1) {
            // 参数错误
            return ResultUtils.error(ResultCode.PARAMS_ERROR);
        } else if (id == -2) {
            // 重复绑定
            return ResultUtils.error(ResultCode.REPEAT_OPERATION);
        }
        // 生成jwt
        Map<String, Object> claims = Map.of("userID", id + "");
        String jwt = JwtUtils.tokenHead + JwtUtils.generateJwt(claims);
        Map<String, Object> map = new HashMap<>();
        map.put("jwt", jwt);
        map.put("user", user);
        map.put("auths", auths);
        return ResultUtils.success(map);
    }
}