package com.himanshu.product_search.search;

import com.himanshu.product_search.product.search.ProductSearchDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/search")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping
    public Page<ProductSearchDocument> search(
            @ModelAttribute SearchRequest request,
            Pageable pageable) {

        return searchService.search(request, pageable);
    }

    @GetMapping("/suggest")
    public Object suggest(
            @RequestParam String q
    ) {
        return searchService.suggest(q);
    }
}
