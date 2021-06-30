package com.tutorial.springbootreactive.services.implementations;

import com.tutorial.springbootreactive.models.Category;
import com.tutorial.springbootreactive.repositories.CategoryRepository;
import com.tutorial.springbootreactive.services.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public Flux<Category> findAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public Mono<Category> findCategoryById(String id) {
        return categoryRepository.findById(id);
    }

    @Override
    public Mono<Category> saveCategory(Category category) {
        return categoryRepository.save(category);
    }

    @Override
    public Mono<Void> deleteCategory(Category category) {
        return categoryRepository.delete(category);
    }
}
