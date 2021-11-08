package com.miya.common.config.web.jwt;

import com.miya.common.config.web.SpringMvcService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

/**
 * @author 杨超辉
 * @date 2018/6/18
 * @description jwt filter
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtRequestResolver jwtRequestResolver;

    @Resource
    private SpringMvcService springMvcService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if ("options".equalsIgnoreCase(request.getMethod())) {
            SecurityContextHolder.getContext().setAuthentication(
                    new AnonymousAuthenticationToken("anonymous", "anonymous",
                            Collections.singletonList(new SimpleGrantedAuthority("anonymous"))));
            return;
        }

        String[] allowAccessUrls = springMvcService.allowAccessUrlForAcl();
        if (Arrays.asList(allowAccessUrls).contains(request.getRequestURI())){
            SecurityContextHolder.getContext().setAuthentication(
                    new AnonymousAuthenticationToken("anonymous", "anonymous",
                            Collections.singletonList(new SimpleGrantedAuthority("anonymous"))));
            chain.doFilter(request, response);
            return;
        }

        Authentication authentication = jwtRequestResolver.getAuthentication(request);
        if (Objects.nonNull(authentication)){
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        chain.doFilter(request, response);
    }
}

