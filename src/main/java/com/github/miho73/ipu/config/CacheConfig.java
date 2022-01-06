package com.github.miho73.ipu.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(final ResourceHandlerRegistry registry) {
        CacheControl cacheControlForYear = CacheControl.maxAge(31536000, TimeUnit.SECONDS)
                                                       .noTransform()
                                                       .mustRevalidate();
        CacheControl cacheControlWeek = CacheControl.maxAge(604800, TimeUnit.SECONDS)
                                                    .noTransform()
                                                    .mustRevalidate();
        CacheControl cacheControlNoCache = CacheControl.noCache()
                                                       .noTransform()
                                                       .mustRevalidate();

        registry.addResourceHandler("**/*.ttf", "**/*.webp", "**/*.svg")
                .addResourceLocations("classpath:/static/")
                .setCacheControl(cacheControlForYear);
        registry.addResourceHandler("**/*.png", "**/*.jpg", "**/problem/lib/*")
                .addResourceLocations("classpath:/static/")
                .setCacheControl(cacheControlWeek);
        registry.addResourceHandler("**/*.css", "**/*.js")
                .addResourceLocations("classpath:/static/")
                .setCacheControl(cacheControlNoCache);
    }
}
