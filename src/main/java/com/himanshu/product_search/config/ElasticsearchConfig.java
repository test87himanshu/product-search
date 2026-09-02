package com.himanshu.product_search.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories(
        basePackages = "com.himanshu.product_search.product.search"
)
public class ElasticsearchConfig {
}
