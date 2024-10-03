package com.miya.common.config.web.jwt;

import com.miya.common.auth.way.GeneralAuthentication;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

/**
 * @author 杨超辉
 * jwt filter
 */
@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtRequestResolver jwtRequestResolver;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if ("options".equalsIgnoreCase(request.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        GeneralAuthentication authentication = jwtRequestResolver.getAuthentication(request);
        if (Objects.nonNull(authentication)){
            request.setAttribute("principal", authentication.getUser());
        }
        chain.doFilter(request, response);
    }
}

