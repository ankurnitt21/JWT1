package com.example.Frontend.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
public class RedirectConfig {

    private static final String API_GATEWAY_IP = "127.0.0.1"; // Replace with your API Gateway's IP
    private static final String API_GATEWAY_URL = "http://localhost:8078"; // Replace with your API Gateway URL

    @Bean
    public FilterRegistrationBean<Filter> redirectFilter() {
        FilterRegistrationBean<Filter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new RedirectFilter());
        registrationBean.addUrlPatterns("/*"); // Apply to all endpoints
        return registrationBean;
    }

    public class RedirectFilter implements Filter {

        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                throws IOException, ServletException {
            System.out.println("Here36");
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            String remoteAddr = httpRequest.getRemoteAddr();
            int remotePort = httpRequest.getRemotePort();
            System.out.println(remoteAddr + ":" + remotePort);
            System.out.println(remoteAddr);

            if (!API_GATEWAY_IP.equals(remoteAddr)) {
                System.out.println("Here37");
                String redirectUrl = API_GATEWAY_URL + httpRequest.getRequestURI();
                httpResponse.sendRedirect(redirectUrl);
            } else {
                System.out.println("Here38");
                chain.doFilter(request, response);
            }
        }

        @Override
        public void init(FilterConfig filterConfig) throws ServletException {
            // Initialization code if needed
        }

        @Override
        public void destroy() {
            // Cleanup code if needed
        }
    }
}