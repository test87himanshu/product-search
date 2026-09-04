package com.himanshu.product_search;

import com.himanshu.product_search.product.search.ProductSearchRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class ProductSearchApplicationTests {

	@MockitoBean
	private ProductSearchRepository productSearchRepository;

	@Test
	void contextLoads() {
	}

}
