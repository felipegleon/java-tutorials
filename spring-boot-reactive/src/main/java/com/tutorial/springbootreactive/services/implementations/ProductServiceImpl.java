package com.tutorial.springbootreactive.services.implementations;

import com.tutorial.springbootreactive.models.Product;
import com.tutorial.springbootreactive.repositories.ProductRepository;
import com.tutorial.springbootreactive.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Override
    public Flux<Product> findAll() {
        return productRepository.findAll();
    }

    @Override
    public Mono<Product> findById(String id) {
        return productRepository.findById(id);
    }

    @Override
    public Mono<Product> saveProduct(Product product) {
        return productRepository.save(product);
    }

    @Override
    public Mono<Void> deleteProduct(Product product) {
        return productRepository.delete(product);
    }
}
