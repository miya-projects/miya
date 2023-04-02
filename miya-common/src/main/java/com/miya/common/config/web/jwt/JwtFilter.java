package com.miya.common.config.web.jwt;

import com.miya.common.auth.way.GeneralAuthentication;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

/**
 * @author 杨超辉
 * jwt filter
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtRequestResolver jwtRequestResolver;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if ("options".equalsIgnoreCase(request.getMethod())) {
            return;
        }

        GeneralAuthentication authentication = jwtRequestResolver.getAuthentication(request);
        if (Objects.nonNull(authentication)){
            request.setAttribute("principal", authentication.getUser());
        }
        chain.doFilter(request, response);
    }
}

