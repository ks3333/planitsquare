package com.planitsquare.holiday.config;


import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

import lombok.RequiredArgsConstructor;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info = @Info(title = "글로벌 휴일 관리 서비스",
                description = "세계 각국의 휴일을 검색 및 관리 할 수 있는 서비스 입니다",
                version = "v1"))
@RequiredArgsConstructor
@Configuration
public class SwaggerConfig {

    @Bean
    public GroupedOpenApi chatOpenApi() {
        String[] paths = {"/api/holiday/**"};

        return GroupedOpenApi.builder()
                .group("글로벌 휴일 관리 서비스 API")
                .pathsToMatch(paths)
                .build();
    }
}
