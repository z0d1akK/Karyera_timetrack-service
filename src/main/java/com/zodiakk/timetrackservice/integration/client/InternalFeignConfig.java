package com.zodiakk.timetrackservice.integration.client;

import com.zodiakk.timetrackservice.common.constants.Headers;
import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

public class InternalFeignConfig {

    @Bean
    public RequestInterceptor internalApiKeyInterceptor(
            @Value("${internal.api-key}") String internalApiKey
    ) {
        return template -> template.header(Headers.INTERNAL_KEY, internalApiKey);
    }
}
