package com.miya.common.config.web.interceptor;

import cn.hutool.json.JSONUtil;
import com.miya.common.model.dto.base.R;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

/**
 * @author 樊超
 * 签名认证访问拦截器
 * 主要为了防止重放攻击
 * todo BS架构好像也没什么应用场景
 */
@Slf4j
public class SignAccessInterceptor implements HandlerInterceptor {

    /**
     * 信息存60秒
     */
    private Long replayAttackTime = 60L;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        try {
            String nonce = request.getHeader("nonce");
            String timestamp = request.getHeader("timestamp");
            String signature = request.getHeader("signature");
            if (Objects.nonNull(nonce) && Objects.nonNull(timestamp) && Objects.nonNull(signature)) {
                //判断随机数是否存在redis 缓存
//                if (Objects.isNull(redisUtil.get(RedisUtil.NONCE + nonce))) {
//                    redisUtil.set(RedisUtil.NONCE + nonce, true, replayAttackTime);
//                    long time = Long.parseLong(timestamp);
//                    //判断请求时间是否在限制时间之内
//                    if (new Date().getTime() - time <= replayAttackTime * 1000) {
//                        String requestURI = request.getRequestURI();
//                        String authorization = request.getHeader("Authorization");
//                        // 不是登陆接口 且 token不为null
//                        String signString = !requestURI.contains("login") && authorization != null ? nonce + authorization + timestamp :
//                                nonce + "" + timestamp;
//                        String encodeSign = SignUtil.encodeSign(signString);
//                        //验证签名是否正确
//                        if (encodeSign.equals(signature)) {
//                            return true;
//                        }
//                    }
//                }
            }
            response.setStatus(403);
            response.getWriter().write(JSONUtil.toJsonStr(R.errorWithMsg("对不起,签名错误！")));
        } catch (Exception e) {
            log.error(ExceptionUtils.getStackTrace(e));

        }

        return false;
    }


}
