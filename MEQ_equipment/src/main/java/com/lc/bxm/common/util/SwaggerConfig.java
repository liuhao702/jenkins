package com.lc.bxm.common.util;
import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import io.swagger.annotations.ApiOperation;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.Parameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .pathMapping("/rest/")//对请求的路径增加rest前缀
                .globalOperationParameters(setHeaderToken())
                .select()
                .apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class)) //只过滤包含有ApiOperation注解的方法
                .paths(PathSelectors.any()) //对所有的路径进行监控
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
        		 .title("MEQ_equipment 版本")
                 .description("配置Api文档")
                 .termsOfServiceUrl("http://localhost:8989/MEQ_equipment/index.html")
                 .contact(new Contact("token", "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJjeWpxIiwiaWQiOiJjeWpxIiwiZXhwIjox"
                 		+ "NTkwMTMyMTI0LCJpYXQiOjE1OTAxMzIxMjMsImp0aSI6ImNjYjRmNjFkLWJhNDMtNGI1YS1hNGM5LWUzZDZlMGY0MjgyMCJ9.czhe0oFWF96uQI05T1v4ivTpT-D4K8waPzTvi_VtNmc ", "huaxi@qq.com"))
                 .version("1.1")
                 .build();
    }

    private List<Parameter> setHeaderToken() {
        ParameterBuilder tokenPar = new ParameterBuilder();
        List<Parameter> pars = new ArrayList<>();
        tokenPar.name("X-AUTH-TOKEN").description("token").modelRef(new ModelRef("string")).parameterType("header").required(false).build();
        pars.add(tokenPar.build());
        return pars;
    }
}