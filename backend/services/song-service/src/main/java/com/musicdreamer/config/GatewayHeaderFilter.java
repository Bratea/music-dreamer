package com.musicdreamer.config;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 从网关转发来的 X-User-Id 头中提取 userId 并注入 Request Attribute，
 * 供 Controller 通过 @RequestAttribute("userId") 使用。
 */
@Component
public class GatewayHeaderFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        String userIdHeader = request.getHeader("X-User-Id");
        if (userIdHeader != null && !userIdHeader.isBlank()) {
            try {
                request.setAttribute("userId", Long.valueOf(userIdHeader));
            } catch (NumberFormatException ignored) {}
        }
        filterChain.doFilter(request, response);
    }
}
