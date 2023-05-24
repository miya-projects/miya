package com.miya.system.listener;

import com.miya.system.config.ProjectConfiguration;
import com.miya.system.listener.event.SystemStartupEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

/**
 * 系统启动监听器
 */
@Slf4j
@Component
public class StartedListener implements ApplicationListener<SystemStartupEvent> {

    @Resource
    private ProjectConfiguration projectConfiguration;

    @Override
    @Async
    public void onApplicationEvent(SystemStartupEvent event) {
        log.trace("trace日志打印正常");
        log.debug("debug日志打印正常");
        log.info("info日志打印正常");
        log.warn("warn日志打印正常");
        // ExpressionParser spelExpressionParser = new SpelExpressionParser();
        // Expression exp = spelExpressionParser.parseExpression("@projectConfig.email");
        // //执行表达式，默认容器是spring本身的容器：ApplicationContext
        // StandardEvaluationContext context = new StandardEvaluationContext();
        // context.setBeanResolver(new BeanFactoryResolver(SpringUtil.getApplicationContext()));
        // Object value = exp.getValue(context);

    }
}
