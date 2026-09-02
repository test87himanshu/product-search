package com.himanshu.product_search.search;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SuggestionService {

    private final ElasticsearchClient elasticsearchClient;

    public SuggestionService(
            ElasticsearchClient elasticsearchClient) {

        this.elasticsearchClient = elasticsearchClient;
    }

    public List<String> suggest(String query) {

        try {

            SearchResponse<Void> response =
                    elasticsearchClient.search(s -> s
                                    .index("products")
                                    .suggest(sg -> sg
                                            .suggesters(
                                                    "product-suggestions",
                                                    cs -> cs
                                                            .prefix(query)
                                                            .completion(c -> c
                                                                    .field("suggest")
                                                                    .size(10)
                                                            )
                                            )
                                    ),
                            Void.class
                    );

            return response.suggest()
                    .get("product-suggestions")
                    .stream()
                    .flatMap(suggestion ->
                            suggestion.completion()
                                    .options()
                                    .stream()
                    )
                    .map(option -> option.text())
                    .toList();

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to fetch product suggestions",
                    e
            );
        }
    }
}
