package com.tutorial.springbootreactive.services;

import com.tutorial.springbootreactive.models.Category;
import com.tutorial.springbootreactive.models.Product;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductService {

    public Flux<Product> findAll();

    public Mono<Product> findById(String id);

    public Mono<Product> saveProduct(Product product);

    public Mono<Void> deleteProduct(Product product);

    public Flux<Category> findAllCategories();

    public Mono<Category> findCategoryById(String id);

    public Mono<Category> saveCategory(Category category);

    public Mono<Void> deleteCategory(Category category);
}
