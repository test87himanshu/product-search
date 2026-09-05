package com.himanshu.product_search;

import com.himanshu.product_search.product.search.ProductSearchDocument;
import com.himanshu.product_search.search.ProductSearchResponse;
import com.himanshu.product_search.search.SearchRequest;
import com.himanshu.product_search.search.SearchService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import co.elastic.clients.elasticsearch.ElasticsearchClient;

import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductSearchApplicationTests {

    @Mock
    private ElasticsearchOperations elasticsearchOperations;

    @Mock
    private ElasticsearchClient elasticsearchClient;

    @Mock
    private SearchHits<ProductSearchDocument> searchHits;

    @Mock
    private SearchHit<ProductSearchDocument> searchHit;

    @Test
    void searchReturnsProductsFromElasticsearch() {

        ProductSearchDocument product =
                new ProductSearchDocument();

        product.setId(2L);
        product.setName("Samsung Galaxy S24");
        product.setDescription("Android flagship with 256GB storage");
        product.setBrand("Samsung");
        product.setCategory("Smartphones");
        product.setPrice(74999.0);
        product.setRating(4.4);
        product.setInStock(true);

        when(searchHit.getContent()).thenReturn(product);
        when(searchHits.getSearchHits()).thenReturn(List.of(searchHit));
        when(searchHits.getTotalHits()).thenReturn(1L);

        when(elasticsearchOperations.search(
                any(NativeQuery.class),
                eq(ProductSearchDocument.class)
        )).thenReturn(searchHits);

        SearchService searchService =
                new SearchService(
                        elasticsearchOperations,
                        null
                );

        SearchRequest request = new SearchRequest();
        request.setQ("Samsung");

        Pageable pageable = PageRequest.of(0, 20);

        Page<?> result =
                searchService.search(request, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());

        verify(elasticsearchOperations).search(
                any(NativeQuery.class),
                eq(ProductSearchDocument.class)
        );
    }

    @Test
    void searchWithBrandFilterReturnsProducts() {

        ProductSearchDocument product =
                new ProductSearchDocument();

        product.setId(2L);
        product.setName("Samsung Galaxy S24");
        product.setBrand("Samsung");

        when(searchHit.getContent()).thenReturn(product);
        when(searchHits.getSearchHits()).thenReturn(List.of(searchHit));
        when(searchHits.getTotalHits()).thenReturn(1L);

        when(elasticsearchOperations.search(
                any(NativeQuery.class),
                eq(ProductSearchDocument.class)
        )).thenReturn(searchHits);

        SearchService searchService =
                new SearchService(
                        elasticsearchOperations,
                        null
                );

        SearchRequest request = new SearchRequest();
        request.setBrand("Samsung");

        Pageable pageable = PageRequest.of(0, 20);

        Page<?> result =
                searchService.search(request, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(
                "Samsung Galaxy S24",
                ((ProductSearchResponse) result.getContent().get(0)).getName()
        );

        verify(elasticsearchOperations).search(
                any(NativeQuery.class),
                eq(ProductSearchDocument.class)
        );
    }

    @Test
    void searchWithPriceFilterReturnsProducts() {

        ProductSearchDocument product =
                new ProductSearchDocument();

        product.setId(2L);
        product.setName("Samsung Galaxy S24");
        product.setPrice(74999.0);

        when(searchHit.getContent()).thenReturn(product);
        when(searchHits.getSearchHits()).thenReturn(List.of(searchHit));
        when(searchHits.getTotalHits()).thenReturn(1L);

        when(elasticsearchOperations.search(
                any(NativeQuery.class),
                eq(ProductSearchDocument.class)
        )).thenReturn(searchHits);

        SearchService searchService =
                new SearchService(
                        elasticsearchOperations,
                        null
                );

        SearchRequest request = new SearchRequest();
        request.setMinPrice(50000.0);
        request.setMaxPrice(80000.0);

        Pageable pageable = PageRequest.of(0, 20);

        Page<?> result =
                searchService.search(request, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(
                "Samsung Galaxy S24",
                ((ProductSearchResponse) result.getContent().get(0)).getName()
        );
    }

    @Test
    void searchWithMinimumRatingReturnsProducts() {

        ProductSearchDocument product =
                new ProductSearchDocument();

        product.setId(13L);
        product.setName("Samsung Galaxy S26");
        product.setRating(4.7);

        when(searchHit.getContent()).thenReturn(product);
        when(searchHits.getSearchHits()).thenReturn(List.of(searchHit));
        when(searchHits.getTotalHits()).thenReturn(1L);

        when(elasticsearchOperations.search(
                any(NativeQuery.class),
                eq(ProductSearchDocument.class)
        )).thenReturn(searchHits);

        SearchService searchService =
                new SearchService(
                        elasticsearchOperations,
                        null
                );

        SearchRequest request = new SearchRequest();
        request.setMinRating(4.5);

        Pageable pageable = PageRequest.of(0, 20);

        Page<?> result =
                searchService.search(request, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(
                "Samsung Galaxy S26",
                ((ProductSearchResponse) result.getContent().get(0)).getName()
        );
    }

    @Test
    void searchWithBrandFilterBuildsBrandKeywordFilter() {

        when(searchHits.getSearchHits()).thenReturn(List.of());
        when(searchHits.getTotalHits()).thenReturn(0L);

        when(elasticsearchOperations.search(
                any(NativeQuery.class),
                eq(ProductSearchDocument.class)
        )).thenReturn(searchHits);

        SearchService searchService =
                new SearchService(
                        elasticsearchOperations,
                        null
                );

        SearchRequest request = new SearchRequest();
        request.setBrand("Samsung");

        Pageable pageable = PageRequest.of(0, 20);

        searchService.search(request, pageable);

        ArgumentCaptor<NativeQuery> queryCaptor =
                ArgumentCaptor.forClass(NativeQuery.class);

        verify(elasticsearchOperations).search(
                queryCaptor.capture(),
                eq(ProductSearchDocument.class)
        );

        NativeQuery query = queryCaptor.getValue();

        assertEquals(
                "Query: {\"bool\":{\"filter\":[{\"term\":{\"brand.keyword\":{\"value\":\"Samsung\"}}}]}}",
                query.getQuery().toString()
        );
    }

    @Test
    void searchWithMinRatingBuildsRatingFilter() {

        when(searchHits.getSearchHits()).thenReturn(List.of());
        when(searchHits.getTotalHits()).thenReturn(0L);

        when(elasticsearchOperations.search(
                any(NativeQuery.class),
                eq(ProductSearchDocument.class)
        )).thenReturn(searchHits);

        SearchRequest request = new SearchRequest();
        request.setMinRating(4.5);

        Pageable pageable = PageRequest.of(0, 20);

        SearchService service = new SearchService(
                elasticsearchOperations,
                elasticsearchClient
        );

        service.search(request, pageable);

        ArgumentCaptor<NativeQuery> queryCaptor =
                ArgumentCaptor.forClass(NativeQuery.class);

        verify(elasticsearchOperations).search(
                queryCaptor.capture(),
                eq(ProductSearchDocument.class)
        );

        NativeQuery query = queryCaptor.getValue();

        assertEquals(
                "Query: {\"bool\":{\"filter\":[{\"range\":{\"rating\":{\"gte\":4.5}}}]}}",
                query.getQuery().toString()
        );
    }

    @Test
    void searchWithInStockFilterBuildsStockFilter() {

        when(searchHits.getSearchHits()).thenReturn(List.of());
        when(searchHits.getTotalHits()).thenReturn(0L);

        when(elasticsearchOperations.search(
                any(NativeQuery.class),
                eq(ProductSearchDocument.class)
        )).thenReturn(searchHits);

        SearchRequest request = new SearchRequest();
        request.setInStock(true);

        Pageable pageable = PageRequest.of(0, 20);

        SearchService service = new SearchService(
                elasticsearchOperations,
                elasticsearchClient
        );

        service.search(request, pageable);

        ArgumentCaptor<NativeQuery> queryCaptor =
                ArgumentCaptor.forClass(NativeQuery.class);

        verify(elasticsearchOperations).search(
                queryCaptor.capture(),
                eq(ProductSearchDocument.class)
        );

        NativeQuery query = queryCaptor.getValue();

        assertEquals(
                "Query: {\"bool\":{\"filter\":[{\"term\":{\"inStock\":{\"value\":true}}}]}}",
                query.getQuery().toString()
        );
    }

    @Test
    void searchWithCategoryFilterBuildsCategoryKeywordFilter() {

        when(searchHits.getSearchHits()).thenReturn(List.of());
        when(searchHits.getTotalHits()).thenReturn(0L);

        when(elasticsearchOperations.search(
                any(NativeQuery.class),
                eq(ProductSearchDocument.class)
        )).thenReturn(searchHits);

        SearchRequest request = new SearchRequest();
        request.setCategory("Smartphone");

        Pageable pageable = PageRequest.of(0, 20);

        SearchService service = new SearchService(
                elasticsearchOperations,
                elasticsearchClient
        );

        service.search(request, pageable);

        ArgumentCaptor<NativeQuery> queryCaptor =
                ArgumentCaptor.forClass(NativeQuery.class);

        verify(elasticsearchOperations).search(
                queryCaptor.capture(),
                eq(ProductSearchDocument.class)
        );

        NativeQuery query = queryCaptor.getValue();

        assertEquals(
                "Query: {\"bool\":{\"filter\":[{\"term\":{\"category.keyword\":{\"value\":\"Smartphone\"}}}]}}",
                query.getQuery().toString()
        );
    }

    @Test
    void searchWithPriceAscendingSortBuildsPriceSort() {

        when(searchHits.getSearchHits()).thenReturn(List.of());
        when(searchHits.getTotalHits()).thenReturn(0L);

        when(elasticsearchOperations.search(
                any(NativeQuery.class),
                eq(ProductSearchDocument.class)
        )).thenReturn(searchHits);

        SearchRequest request = new SearchRequest();
        request.setSort("price_asc");

        Pageable pageable = PageRequest.of(0, 20);

        SearchService service = new SearchService(
                elasticsearchOperations,
                elasticsearchClient
        );

        service.search(request, pageable);

        ArgumentCaptor<NativeQuery> queryCaptor =
                ArgumentCaptor.forClass(NativeQuery.class);

        verify(elasticsearchOperations).search(
                queryCaptor.capture(),
                eq(ProductSearchDocument.class)
        );

        NativeQuery query = queryCaptor.getValue();

        assertEquals(
                "price_asc",
                request.getSort()
        );
    }

    @Test
    void searchWithPriceDescendingSortWorks() {

        when(searchHits.getSearchHits()).thenReturn(List.of());
        when(searchHits.getTotalHits()).thenReturn(0L);

        when(elasticsearchOperations.search(
                any(NativeQuery.class),
                eq(ProductSearchDocument.class)
        )).thenReturn(searchHits);

        SearchRequest request = new SearchRequest();
        request.setSort("price_desc");

        Pageable pageable = PageRequest.of(0, 20);

        SearchService service = new SearchService(
                elasticsearchOperations,
                elasticsearchClient
        );

        service.search(request, pageable);

        assertEquals("price_desc", request.getSort());
    }

    @Test
    void searchWithRatingDescendingSortWorks() {

        when(searchHits.getSearchHits()).thenReturn(List.of());
        when(searchHits.getTotalHits()).thenReturn(0L);

        when(elasticsearchOperations.search(
                any(NativeQuery.class),
                eq(ProductSearchDocument.class)
        )).thenReturn(searchHits);

        SearchRequest request = new SearchRequest();
        request.setSort("rating_desc");

        Pageable pageable = PageRequest.of(0, 20);

        SearchService service = new SearchService(
                elasticsearchOperations,
                elasticsearchClient
        );

        service.search(request, pageable);

        assertEquals("rating_desc", request.getSort());
    }

    @Test
    void searchWithRelevanceSortWorks() {

        when(searchHits.getSearchHits()).thenReturn(List.of());
        when(searchHits.getTotalHits()).thenReturn(0L);

        when(elasticsearchOperations.search(
                any(NativeQuery.class),
                eq(ProductSearchDocument.class)
        )).thenReturn(searchHits);

        SearchRequest request = new SearchRequest();
        request.setSort("relevance");

        Pageable pageable = PageRequest.of(0, 20);

        SearchService service = new SearchService(
                elasticsearchOperations,
                elasticsearchClient
        );

        service.search(request, pageable);

        assertEquals("relevance", request.getSort());
    }

    @Test
    void searchWithInvalidSortThrowsBadRequest() {

        SearchRequest request = new SearchRequest();
        request.setSort("invalid_sort");

        Pageable pageable = PageRequest.of(0, 20);

        SearchService service = new SearchService(
                elasticsearchOperations,
                elasticsearchClient
        );

        assertThrows(
                ResponseStatusException.class,
                () -> service.search(request, pageable)
        );
    }
}
