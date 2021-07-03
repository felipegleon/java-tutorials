package com.tutorial.springwebfluxresfull.repositories;

import com.tutorial.springwebfluxresfull.models.Product;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface ProductRepository extends ReactiveMongoRepository<Product, String> {

    Mono<Product> findFirstByName(String name);
}
