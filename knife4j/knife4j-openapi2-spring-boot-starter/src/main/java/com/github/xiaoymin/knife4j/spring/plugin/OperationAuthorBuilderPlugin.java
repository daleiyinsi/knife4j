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


package com.github.xiaoymin.knife4j.spring.plugin;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import springfox.documentation.service.StringVendorExtension;
import springfox.documentation.service.VendorExtension;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.OperationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/***
 *
 * @since  1.0
 * @author <a href="mailto:xiaoymin@foxmail.com">xiaoymin@foxmail.com</a> 
 * 2019/06/06 20:16
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 101)
public class OperationAuthorBuilderPlugin extends AbstractOperationBuilderPlugin {
    
    /***
     * 添加作者属性
     * @param context 接口上下文
     */
    @Override
    public void apply(OperationContext context) {
        Optional<ApiOperationSupport> apiOperationSupportOptional = context.findAnnotation(ApiOperationSupport.class);
        if (apiOperationSupportOptional.isPresent()) {
            String author = apiOperationSupportOptional.get().author();
            // 判断非空
            if (author != null && !"".equals(author) && !"null".equals(author)) {
                List<VendorExtension> vendorExtensions = new ArrayList<>();
                vendorExtensions.add(new StringVendorExtension("x-author", author));
                context.operationBuilder().extensions(vendorExtensions);
            }
        }
    }
    
    @Override
    public boolean supports(DocumentationType delimiter) {
        return true;
    }
}
