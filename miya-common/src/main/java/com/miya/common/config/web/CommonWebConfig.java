package com.miya.common.config.web;

import cn.hutool.core.convert.Convert;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.miya.common.config.web.interceptor.ActionLogInterceptor;
import com.miya.common.config.web.interceptor.ApiRequestLimitInterceptor;
import com.miya.common.config.web.interceptor.ApiUsageLimitInterceptor;
import com.miya.common.config.web.jwt.JwtFilter;
import com.miya.common.model.dto.base.R;
import com.miya.common.model.dto.base.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.format.FormatterRegistry;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.vote.AuthenticatedVoter;
import org.springframework.security.access.vote.UnanimousBased;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.expression.WebExpressionVoter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 每创建一个WebSecurityConfigurerAdapter子类对象，就会创建一个filter chain
 * https://blog.csdn.net/linzhiqiang0316/article/details/78358907
 */
@Configuration
// @Order(SecurityProperties.DEFAULT_FILTER_ORDER)
@EnableWebSecurity
@EnableSpringDataWebSupport
@Slf4j
public class CommonWebConfig extends WebSecurityConfigurerAdapter implements WebMvcConfigurer {

    @Resource
    private ActionLogInterceptor actionLogInterceptor;
    @Resource
    private AuthenticationManager authenticationManager;
    @Resource
    private Optional<RoleBasedVoter> roleBasedVoterOptional;
    @Resource
    private SpringMvcService springMvcService;
    @Resource
    private JwtFilter jwtAuthFilter;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                //关闭frame限制策略
                .headers().frameOptions().disable()
                //关闭csrf
                .and().csrf().disable()
                //跨域配置
                .cors().configurationSource(configurationSource())
                //配置session策略
                .and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                //非api开头的请求允许匿名
                .and().authorizeRequests()
                .regexMatchers("^(?!/api/).+").permitAll()
                .antMatchers(springMvcService.allowAccessUrlForAcl()).permitAll()
                .anyRequest().authenticated()
                .and()
                .authorizeRequests().accessDecisionManager(accessDecisionManager())
                .and()
                .logout().logoutUrl("/api/logout")
                .logoutSuccessHandler((request,response,authentication) -> {
                    request.getSession().invalidate();
                    response.setContentType("application/json");
                    response.getWriter().write(JSONUtil.toJsonStr(R.success()));
                })
                .and()
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling()
                .authenticationEntryPoint(this.unauthorizedHandler())
                .accessDeniedHandler(this.accessDeniedHandler());
    }

    /**
     * 未登录的处理办法
     * @return
     */
    private AuthenticationEntryPoint unauthorizedHandler(){
        return (request, response, authException) -> {
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json");
            R<?> r = R.errorWithCodeAndMsg(ResponseCode.Common.NO_LOGIN);
            response.getWriter().println(JSONUtil.toJsonStr(r));
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().flush();
        };
    }

    /**
     * 无权限的处理办法
     * @return
     */
    private AccessDeniedHandler accessDeniedHandler(){
        return (request, response, accessDeniedException) -> {
            log.warn("enter accessDeniedHandler...");
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json");
            R<?> r = R.errorWithCodeAndMsg(ResponseCode.Common.NO_PERMISSION);
            response.getWriter().println(JSONUtil.toJsonStr(r));
            response.getWriter().flush();
        };
    }

    /**
     * cors配置
     * @return
     */
    @Bean
    public UrlBasedCorsConfigurationSource configurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOriginPattern("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        //缓存时间，即上一次发送options请求后，相同url过多长时间再发请求才会再次发送options请求预检，时间段内不会发送options预检
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Override
    protected AuthenticationManager authenticationManager() {
        return authenticationManager;
    }

    /**
     * 配置上传虚拟路径 webapp目录
     * @param registry 资源处理器注册对象
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//        registry.addResourceHandler("/public/**").addResourceLocations(ResourceUtils.CLASSPATH_URL_PREFIX + "/public/");
    }

    /**
     * 添加json参数解析器
     * @param argumentResolvers
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(new RequestJsonHandlerMethodArgumentResolver());
    }

    /**
     * 增加日期类型转换器
     * @param registry
     */
    @Override
    public void addFormatters(FormatterRegistry registry) {
        //这里改为lambda会报错
        Converter<String, Date> converter = new Converter<String, Date>() {
            @Override
            public Date convert(String source) {
                return Convert.toDate(source);
            }
        };
        Converter<String, Timestamp> timeStampConverter = new Converter<String, Timestamp>() {
            @Override
            public Timestamp convert(String source) {
                return Optional.ofNullable(Convert.toLocalDateTime(source)).map(Timestamp::valueOf).orElse(null);
            }
        };
        Converter<String, LocalDate> localDateConverter = new Converter<String, LocalDate>() {
            @Override
            public LocalDate convert(String source) {
                LocalDateTime localDateTime = Convert.toLocalDateTime(source);
                if (localDateTime == null){
                    return null;
                }
                return LocalDate.of(localDateTime.getYear(), localDateTime.getMonthValue(), localDateTime.getDayOfMonth());
            }
        };
        registry.addConverter(converter);
        registry.addConverter(timeStampConverter);
        registry.addConverter(localDateConverter);
    }


    /**
     * 添加拦截器
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //添加日志拦截器
        registry.addInterceptor(this.actionLogInterceptor).addPathPatterns(Collections.singletonList("/api/**"));
        //api访问次数限制拦截器，限制ip
        registry.addInterceptor(apiUsageLimitInterceptor()).addPathPatterns(Collections.singletonList("/api/**"));
        //单个api访问次数限制拦截器，限制用户
        registry.addInterceptor(new ApiRequestLimitInterceptor()).addPathPatterns(Collections.singletonList("/api/**"));
    }

    @Bean
    public AccessDecisionManager accessDecisionManager() {
        List<AccessDecisionVoter<?>> decisionVoters = new ArrayList<>();
        roleBasedVoterOptional.ifPresent(decisionVoters::add);
        decisionVoters.addAll(Arrays.asList(
                new WebExpressionVoter(),
                new AuthenticatedVoter()));
        return new UnanimousBased(decisionVoters);
    }

    @Bean
    @ConditionalOnMissingBean(ApiUsageLimitInterceptor.class)
    public ApiUsageLimitInterceptor apiUsageLimitInterceptor(){
        return new ApiUsageLimitInterceptor();
    }

    @Bean
    @ConditionalOnMissingBean
    public ActionLogInterceptor actionLogInterceptor(ObjectMapper objectMapper){
        return new ActionLogInterceptor(objectMapper, false, new String[]{"com.miya.system.module.common.MonitorAndMaintenanceApi"});
    }

}
