package com.gym.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final LicenseInterceptor licenseInterceptor;

    public WebConfig(LicenseInterceptor licenseInterceptor) {
        this.licenseInterceptor = licenseInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(licenseInterceptor)
                .addPathPatterns("/admin/**", "/staff/**", "/customer/**")
                .excludePathPatterns("/*/login", "/*/logout", "/*/register", "/admin/license");
    }
}
