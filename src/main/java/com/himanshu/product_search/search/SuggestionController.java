package com.himanshu.product_search.search;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/search/suggestions")
public class SuggestionController {

    private final SuggestionService suggestionService;

    public SuggestionController(
            SuggestionService suggestionService) {

        this.suggestionService = suggestionService;
    }

    @GetMapping
    public List<String> suggest(
            @RequestParam String q) {

        return suggestionService.suggest(q);
    }
}
