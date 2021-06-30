package com.tutorial.springbootreactive.services;

import com.tutorial.springbootreactive.models.Category;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CategoryService {

    public Flux<Category> findAllCategories();

    public Mono<Category> findCategoryById(String id);

    public Mono<Category> saveCategory(Category category);

    public Mono<Void> deleteCategory(Category category);
}
