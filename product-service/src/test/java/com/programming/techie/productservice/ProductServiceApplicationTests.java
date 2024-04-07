package com.programming.techie.productservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mongodb.assertions.Assertions;
import com.programming.techie.productservice.dto.ProductRequest;
import com.programming.techie.productservice.dto.ProductResponse;
import com.programming.techie.productservice.model.Product;
import com.programming.techie.productservice.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testcontainers.shaded.org.hamcrest.CoreMatchers;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
class ProductServiceApplicationTests {


	@Container
	static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.4.2");

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private ProductRepository productRepository;

	@DynamicPropertySource
	static void setProperties(DynamicPropertyRegistry dynamicPropertyRegistry){
		dynamicPropertyRegistry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);

	}

	@Test
	void shouldCreateProduct() throws Exception {
		ProductRequest productRequest = getProductRequest();
		String productRequestString = objectMapper.writeValueAsString(productRequest);

		mockMvc.perform(MockMvcRequestBuilders.post("/api/product")
				.contentType(MediaType.APPLICATION_JSON)
				.content(productRequestString))
				.andExpect(status().isCreated());

//		Assertions.assertTrue(productRepository.findAll().size() ==1 );
	}

	private ProductRequest getProductRequest() {
		return ProductRequest.builder().name("iPhone13")
				.description("iphone 13")
				.price(BigDecimal.valueOf(1300))
				.build();
	}


	@Test
	void shouldGetProduct() throws Exception {
		//given
		List<Product> productResponseList = getProductResponseList();
		productResponseList.stream().forEach(a->productRepository.save(a));
//		String productResponseString = objectMapper.writeValueAsString(productResponseList);
		//when and then
		mockMvc.perform(MockMvcRequestBuilders.get("/api/product")
				.accept(MediaType.APPLICATION_JSON)
//				.content(productResponseString)
		).andDo(print()).andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray()) // Assert that the response is an array
		.andExpect(jsonPath("$.size()").value(productResponseList.size()))
				.andExpect(jsonPath("$[0].id").exists()) // Example assertion: Check if the first product's ID exists
				.andExpect(jsonPath("$[0].name").exists()) // Example assertion: Check if the first product's name exists
				.andExpect(jsonPath("$[0].price").exists()); // Example assertion: Check if the first product's price exists

	}

	private List<Product> getProductResponseList() {

		List<Product> productResponseList = List.of(Product.builder()
				.name("iPhone13")
				.description("iphone 13")
				.price(BigDecimal.valueOf(1300))
				.build(), Product.builder()
				.name("iPhone14")
				.description("iphone 14")
				.price(BigDecimal.valueOf(1400))
				.build());
		return  productResponseList;
//		return productRepository.findAll().stream().map(this::mapToProductResponse).collect(Collectors.toList());
	}

	private ProductResponse mapToProductResponse(Product product) {
		return ProductResponse.builder().name(product.getName())
				.description(product.getDescription())
				.price(product.getPrice()).build();

	}
}
