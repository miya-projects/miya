package com.miya.common.config.web.interceptor;

import cn.hutool.json.JSONUtil;
import com.miya.common.model.dto.base.R;
import com.miya.common.model.dto.base.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 杨超辉
 * @date 2019/10/19
 * @description api访问次数限制拦截器，针对于系统内所有接口
 * 采用滑动窗口的形式限制同一ip访问次数，如设置每分钟最多访问2次，访问记录为09:30:04,09:30:30
 * 即09:31:05才可进行第三次访问，且09:31:31才可进行第四次访问
 */
@Slf4j
public class ApiUsageLimitInterceptor implements HandlerInterceptor {

    /**
     * 访问限制次数
     */
    private Integer visitTimes = 500;

    /**
     * 滑动窗口大小，即在此时间间隔内限制访问visitTimes次，单位：秒
     */
    private Integer windowSize = 60;

    /**
     * 用户访问记录
     * 数据格式：
     * [{
     * ip: [09:30,09:31,09:35](队列)
     * }]
     */
    private final Map<String, Queue<Date>> accessRecords = new ConcurrentHashMap<>();

    public ApiUsageLimitInterceptor() {
    }

    public ApiUsageLimitInterceptor(Integer visitTimes, Integer windowSize) {
        this.visitTimes = visitTimes;
        this.windowSize = windowSize;
    }


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object o) throws IOException {
        String ip = request.getRemoteHost();
        Queue<Date> accessQueue = accessRecords.get(ip);
        if (Objects.isNull(accessQueue)) {
            accessQueue = new ArrayBlockingQueue<>(visitTimes);
            accessRecords.put(ip, accessQueue);
        }
        //队列未满
        if (visitTimes.compareTo(accessQueue.size()) >= 0) {
            return ApiRequestLimitInterceptor.mm(windowSize, accessQueue);
        }
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        response.getWriter().print(JSONUtil.toJsonStr(R.errorWithCodeAndMsg(ResponseCode.Common.VISIT_TOO_FAST)));
        return false;
    }

}
