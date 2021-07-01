package com.tutorial.springwebfluxresfull.services;

import com.tutorial.springwebfluxresfull.models.Category;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CategoryService {

    public Flux<Category> findAllCategories();

    public Mono<Category> findCategoryById(String id);

    public Mono<Category> saveCategory(Category category);

    public Mono<Void> deleteCategory(Category category);
}
