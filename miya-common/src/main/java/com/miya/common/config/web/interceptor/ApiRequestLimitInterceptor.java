package com.miya.common.config.web.interceptor;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.miya.common.annotation.RequestLimit;
import com.miya.common.model.dto.base.R;
import com.miya.common.model.dto.base.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.util.CastUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.Date;
import java.util.Objects;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * 单个接口请求次数限制拦截器，如发送短信验证码，整点抢购等业务场景 不支持分布式
 * 可在接口方法上增加 @see com.miya.system.annotation.RequestLimit实现m分钟内只能访问n次
 */
@Slf4j
public class ApiRequestLimitInterceptor implements HandlerInterceptor{

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        RequestLimit requestLimit = AnnotationUtils.findAnnotation(((HandlerMethod) handler).getMethod(), RequestLimit.class);
        if (Objects.isNull(requestLimit)){
            return true;
        }
        int count = requestLimit.count();
        int seconds = requestLimit.seconds();

        HttpSession session = request.getSession();
        String requestURI = request.getRequestURI();
        String key = "requestLimit_" + requestURI;
        Object o = session.getAttribute(key);
        Queue<Date> accessQueue;
        if (Objects.isNull(o)){
            accessQueue = new ArrayBlockingQueue<>(count);
            session.setAttribute(key, accessQueue);
        }else {
            accessQueue = CastUtils.cast(o);
        }

        //队列未满
        if (count > accessQueue.size() ) {
            addToQueue(seconds, accessQueue);
            return true;
        }
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        response.setStatus(429);
        response.getWriter().print(JSONUtil.toJsonStr(R.errorWithCodeAndMsg(ResponseCode.Common.VISIT_TOO_FAST)));
        return false;
    }

    /**
     * 队列未满，加到队列里去
     * @param seconds   多少秒后过期
     * @param accessQueue   访问队列
     */
    public synchronized static void addToQueue(int seconds, Queue<Date> accessQueue) {
        accessQueue.add(new Date());
        final Queue<Date> accessQueueForLambada = accessQueue;
        Timer timer = new Timer();
        TimerTask task = new TimerTask(){
            @Override
            public void run() {
                accessQueueForLambada.poll();
            }
        };
        Date now = new Date();
        timer.schedule(task, DateUtil.offsetSecond(now, seconds));
    }

}
