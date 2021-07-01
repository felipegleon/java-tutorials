package com.tutorial.springwebfluxresfull.repositories;

import com.tutorial.springwebfluxresfull.models.Category;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends ReactiveMongoRepository<Category, String> {
}
