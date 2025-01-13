package com.example.gateway.Api.Gateway;

import com.example.gateway.Api.Gateway.filter.JwtValidationFilter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;

@SpringBootApplication
@ComponentScan(basePackages = "com.example.gateway.Api.Gateway")
@EnableDiscoveryClient
public class ApiGatewayApplication {

	private final Environment environment;

    public ApiGatewayApplication(Environment environment) {
        this.environment = environment;
    }

    public static void main(String[] args) {
		SpringApplication.run(ApiGatewayApplication.class, args);
	}


}
