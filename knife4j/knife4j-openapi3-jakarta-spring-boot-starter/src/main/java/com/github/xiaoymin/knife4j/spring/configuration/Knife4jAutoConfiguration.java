/*
 * Copyright 2017-2022 八一菜刀(xiaoymin@foxmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.github.xiaoymin.knife4j.spring.configuration;

import com.github.xiaoymin.knife4j.spring.extension.Knife4jOpenApiCustomizer;
import com.github.xiaoymin.knife4j.spring.filter.ProductionSecurityFilter;
import com.github.xiaoymin.knife4j.spring.filter.SecurityBasicAuthFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/***
 * Knife4j 基础自动配置类
 * {@code @since } 2.0.0
 * @author <a href="mailto:xiaoymin@foxmail.com">xiaoymin@foxmail.com</a>
 * 2019/08/28 21:08
 */
@Configuration
@EnableConfigurationProperties({Knife4jProperties.class, Knife4jSetting.class})
@ConditionalOnProperty(name = "knife4j.enable", havingValue = "true")
public class Knife4jAutoConfiguration {
    
    @Autowired
    private Environment environment;
    Logger logger = LoggerFactory.getLogger(Knife4jAutoConfiguration.class);
    /**
     * 增强自定义配置
     * @param knife4jProperties
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public Knife4jOpenApiCustomizer knife4jOpenApiCustomizer(Knife4jProperties knife4jProperties) {
        logger.debug("Register Knife4jOpenApiCustomizer");
        return new Knife4jOpenApiCustomizer(knife4jProperties);
    }
    /**
     * 配置Cors
     *
     * @since 2.0.4
     */
    @Bean("knife4jCorsFilter")
    @ConditionalOnMissingBean(CorsFilter.class)
    @ConditionalOnProperty(name = "knife4j.cors", havingValue = "true")
    public CorsFilter corsFilter() {
        logger.info("init CorsFilter...");
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.addAllowedOrigin("*");
        corsConfiguration.addAllowedHeader("*");
        corsConfiguration.addAllowedMethod("*");
        corsConfiguration.setMaxAge(10000L);
        // 匹配所有API
        source.registerCorsConfiguration("/**", corsConfiguration);
        CorsFilter corsFilter = new CorsFilter(source);
        return corsFilter;
    }
    
    @Bean
    @ConditionalOnMissingBean(SecurityBasicAuthFilter.class)
    @ConditionalOnProperty(name = "knife4j.basic.enable", havingValue = "true")
    public SecurityBasicAuthFilter securityBasicAuthFilter(Knife4jProperties knife4jProperties) {
        boolean enableSwaggerBasicAuth = false;
        String dftUserName = "admin", dftPass = "123321";
        SecurityBasicAuthFilter securityBasicAuthFilter = null;
        if (knife4jProperties == null) {
            if (environment != null) {
                String enableAuth = environment.getProperty("knife4j.basic.enable");
                enableSwaggerBasicAuth = Boolean.valueOf(enableAuth);
                if (enableSwaggerBasicAuth) {
                    // 如果开启basic验证,从配置文件中获取用户名和密码
                    String pUser = environment.getProperty("knife4j.basic.username");
                    String pPass = environment.getProperty("knife4j.basic.password");
                    if (pUser != null && !"".equals(pUser)) {
                        dftUserName = pUser;
                    }
                    if (pPass != null && !"".equals(pPass)) {
                        dftPass = pPass;
                    }
                }
                securityBasicAuthFilter = new SecurityBasicAuthFilter(enableSwaggerBasicAuth, dftUserName, dftPass);
            }
        } else {
            // 判断非空
            if (knife4jProperties.getBasic() == null) {
                securityBasicAuthFilter = new SecurityBasicAuthFilter(enableSwaggerBasicAuth, dftUserName, dftPass);
            } else {
                securityBasicAuthFilter = new SecurityBasicAuthFilter(knife4jProperties.getBasic().isEnable(), knife4jProperties.getBasic().getUsername(), knife4jProperties.getBasic().getPassword());
            }
        }
        return securityBasicAuthFilter;
    }
    
    @Bean
    @ConditionalOnMissingBean(ProductionSecurityFilter.class)
    @ConditionalOnProperty(name = "knife4j.production", havingValue = "true")
    public ProductionSecurityFilter productionSecurityFilter(Knife4jProperties knife4jProperties) {
        boolean prod = false;
        ProductionSecurityFilter p = null;
        if (knife4jProperties == null) {
            if (environment != null) {
                String prodStr = environment.getProperty("knife4j.production");
                if (logger.isDebugEnabled()) {
                    logger.debug("swagger.production:{}", prodStr);
                }
                prod = Boolean.valueOf(prodStr);
            }
            p = new ProductionSecurityFilter(prod);
        } else {
            p = new ProductionSecurityFilter(knife4jProperties.isProduction());
        }
        
        return p;
    }
    
}
