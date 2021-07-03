package com.tutorial.springwebfluxresfull;

import com.tutorial.springwebfluxresfull.models.Category;
import com.tutorial.springwebfluxresfull.models.Product;
import com.tutorial.springwebfluxresfull.services.ProductService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.Collections;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SpringWebfluxResfullApplicationTests {

	private static Logger LOG = LoggerFactory.getLogger(SpringWebfluxResfullApplicationTests.class);

	@Autowired
	private WebTestClient client;

	@Autowired
	private ProductService productService;

	@Test
	public void findAllProducts() {
		LOG.info("Run Test findAllProducts");
		client.get()
				.uri("/handler/api/products")
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isOk()
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBodyList(Product.class);
	}

	@Test
	public void findProductById() {
		LOG.info("Run Test findProductById");
		client.get()
				.uri("/handler/api/products/{id}", Collections.singletonMap("id", "60dbe8199063e7117f8af760"))
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isOk()
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBody(Product.class)
				.consumeWith(response -> {
					Product product = response.getResponseBody();
					Assertions.assertNotNull(product.getId());
				});
	}

	@Test
	public void createProduct(){
		LOG.info("Run Test createProduct");
		Product product =  new Product("60e0c4b75852646ce2d34f18", "Test", 100.00, null, new Category("60dbda2f837782749322a228", "Technology"), null);
		client.post()
				.uri("/handler/api/products")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.body(Mono.just(product), Product.class)
				.exchange()
				.expectStatus().isCreated()
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBody(Product.class)
				.consumeWith(response -> {
					Product p =  response.getResponseBody();
					Assertions.assertNotNull(p.getId());
					Assertions.assertNotNull(p.getName());
					Assertions.assertNotNull(p.getCategory());
				});
				//.jsonPath("$.id").isNotEmpty()
				//.jsonPath("$.name").isNotEmpty();
	}

	@Test
	public void editProduct (){
		LOG.info("Run Test editProduct");
		Product editedProduct =  new Product(null, "ChangeName", 120.12, null, new Category("60dbda71837782749322a22b", "Car"), null);
		client.put()
				.uri("/handler/api/products/{id}", Collections.singletonMap("id", "60e0c4b75852646ce2d34f18"))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.body(Mono.just(editedProduct), Product.class)
				.exchange()
				.expectStatus().isOk()
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBody()
				.jsonPath("$.id").isNotEmpty()
				.jsonPath("$.name").isEqualTo("ChangeName")
				.jsonPath("$.category.name").isEqualTo("Car");
	}

	@Test
	public void deleteProduct (){
		LOG.info("Run Test deleteProduct");
		client.delete()
				.uri("/handler/api/products/{id}", Collections.singletonMap("id", "60e0c4b75852646ce2d34f18"))
				.exchange()
				.expectStatus().isNoContent()
				.expectBody().isEmpty();

		client.get()
				.uri("/handler/api/products/{id}", Collections.singletonMap("id", "60e0c4b75852646ce2d34f18"))
				.exchange()
				.expectStatus().isNotFound()
				.expectBody().isEmpty();
	}
}
