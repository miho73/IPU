package com.github.miho73.ipu.config.cache;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfigWeak implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(final ResourceHandlerRegistry registry) {
        CacheControl cacheControl = CacheControl.maxAge(604800, TimeUnit.SECONDS)
                                                .noTransform()
                                                .mustRevalidate();

        registry.addResourceHandler("**/*.png", "**/*.jpg")
                .addResourceLocations("classpath:/static/")
                .setCacheControl(cacheControl);
    }
}
