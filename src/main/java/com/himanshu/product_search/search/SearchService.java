package com.himanshu.product_search.search;

import com.himanshu.product_search.product.search.ProductSearchDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;

import java.util.ArrayList;
import java.util.List;

@Service
public class SearchService {

    private final ElasticsearchOperations elasticsearchOperations;
    private final ElasticsearchClient elasticsearchClient;

    public SearchService(
            ElasticsearchOperations elasticsearchOperations,
            ElasticsearchClient elasticsearchClient
    ) {
        this.elasticsearchOperations = elasticsearchOperations;
        this.elasticsearchClient = elasticsearchClient;
    }

    public Page<ProductSearchDocument> search(
            SearchRequest request,
            Pageable pageable) {

        List<SortOptions> sortOptions = new ArrayList<>();

        if (request.getSort() != null) {

            switch (request.getSort()) {

                case "relevance" -> sortOptions.add(
                        SortOptions.of(s -> s
                                .score(sc -> sc.order(SortOrder.Desc))
                        )
                );

                case "price_asc" -> sortOptions.add(
                        SortOptions.of(s -> s
                                .field(f -> f
                                        .field("price")
                                        .order(SortOrder.Asc)
                                )
                        )
                );

                case "price_desc" -> sortOptions.add(
                        SortOptions.of(s -> s
                                .field(f -> f
                                        .field("price")
                                        .order(SortOrder.Desc)
                                )
                        )
                );

                case "rating_desc" -> sortOptions.add(
                        SortOptions.of(s -> s
                                .field(f -> f
                                        .field("rating")
                                        .order(SortOrder.Desc)
                                )
                        )
                );

                default -> throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Unsupported sort option: " + request.getSort()
                );
            }
        }

        NativeQueryBuilder queryBuilder = NativeQuery.builder()
                .withQuery(q -> q
                        .bool(b -> {

                            if (request.getQ() != null &&
                                    !request.getQ().isBlank()) {

                                b.must(m -> m
                                        .multiMatch(mm -> mm
                                                .query(request.getQ())
                                                .fields(
                                                        "name^3",
                                                        "brand^2",
                                                        "category^1.5",
                                                        "description"
                                                )
                                                .fuzziness("AUTO")
                                        )
                                );
                            }

                            if (request.getBrand() != null &&
                                    !request.getBrand().isBlank()) {

                                b.filter(f -> f
                                        .term(t -> t
                                                .field("brand.keyword")
                                                .value(request.getBrand())
                                        )
                                );
                            }

                            if (request.getCategory() != null &&
                                    !request.getCategory().isBlank()) {

                                b.filter(f -> f
                                        .term(t -> t
                                                .field("category.keyword")
                                                .value(request.getCategory())
                                        )
                                );
                            }

                            if (request.getMinPrice() != null ||
                                    request.getMaxPrice() != null) {

                                b.filter(f -> f
                                        .range(r -> r
                                                .number(n -> {
                                                    n.field("price");
                                                    if (request.getMinPrice() != null) {
                                                        n.gte(request.getMinPrice());
                                                    }

                                                    if (request.getMaxPrice() != null) {
                                                        n.lte(request.getMaxPrice());
                                                    }

                                                    return n;
                                                })
                                        )
                                );
                            }

                            if (request.getMinRating() != null) {

                                b.filter(f -> f
                                        .range(r -> r
                                                .number(n -> n
                                                        .field("rating")
                                                        .gte(request.getMinRating())
                                                )
                                        )
                                );
                            }

                            if (request.getInStock() != null) {

                                b.filter(f -> f
                                        .term(t -> t
                                                .field("inStock")
                                                .value(request.getInStock())
                                        )
                                );
                            }

                            return b;
                        })
                );

        if (!sortOptions.isEmpty()) {
            queryBuilder.withSort(sortOptions);
        }

        NativeQuery searchQuery = queryBuilder
                .withPageable(PageRequest.of(pageable.getPageNumber(), pageable.getPageSize()))
                .build();

        SearchHits<ProductSearchDocument> searchHits =
                elasticsearchOperations.search(
                        searchQuery,
                        ProductSearchDocument.class
                );

        List<ProductSearchDocument> products =
                searchHits.getSearchHits()
                        .stream()
                        .map(SearchHit::getContent)
                        .toList();

        return new PageImpl<>(
                products,
                pageable,
                searchHits.getTotalHits()
        );
    }

    public List<String> suggest(String query) {

        try {
            SearchResponse<Void> response =
                    elasticsearchClient.search(s -> s
                                    .index("products")
                                    .suggest(sg -> sg
                                            .suggesters("product-suggestions", cs -> cs
                                                    .prefix(query)
                                                    .completion(c -> c
                                                            .field("suggest")
                                                    )
                                            )
                                    ),
                            Void.class
                    );

            return response.suggest()
                    .get("product-suggestions")
                    .stream()
                    .flatMap(suggestion ->
                            suggestion.completion().options().stream()
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
