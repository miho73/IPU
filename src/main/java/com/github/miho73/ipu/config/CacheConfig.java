package com.github.miho73.ipu.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CacheConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(final ResourceHandlerRegistry registry) {
        CacheControl cacheControl = CacheControl
                .noCache()
                .mustRevalidate();

        registry.addResourceHandler("/*")
                .setCacheControl(cacheControl);
    }
}