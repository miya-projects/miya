package com.miya.common.config.web;

import cn.hutool.json.JSONUtil;
import com.miya.common.config.web.jwt.JwtFilter;
import com.miya.common.model.dto.base.R;
import com.miya.common.model.dto.base.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.vote.AuthenticatedVoter;
import org.springframework.security.access.vote.UnanimousBased;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.expression.WebExpressionVoter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * 每创建一个WebSecurityConfigurerAdapter子类对象，就会创建一个filter chain
 * https://blog.csdn.net/linzhiqiang0316/article/details/78358907
 */
@Configuration
@Order(SecurityProperties.DEFAULT_FILTER_ORDER)
@Slf4j
public class CommonWebSecurityConfig extends WebSecurityConfigurerAdapter {


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
                .cors(withDefaults())
                //配置session策略
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
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

    // 使用token时，避免了csrf，可以允许跨域
    // @Bean
    // public CorsConfigurationSource corsConfigurationSource(){
    //     CorsConfiguration config = new CorsConfiguration();
    //     config.setAllowCredentials(true);
    //     config.addAllowedOriginPattern(CorsConfiguration.ALL);
    //     config.addAllowedHeader(CorsConfiguration.ALL);
    //     config.addAllowedMethod(CorsConfiguration.ALL);
    //     //缓存时间，即上一次发送options请求后，相同url过多长时间再发请求才会再次发送options请求预检，时间段内不会发送options预检
    //     config.setMaxAge(3600L);
    //     UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    //     source.registerCorsConfiguration("/**", config);
    //     return source;
    // }

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


    @Override
    protected AuthenticationManager authenticationManager() {
        return authenticationManager;
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

}
