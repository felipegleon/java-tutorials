package com.tutorial.springbootreactive.controllers;

import com.tutorial.springbootreactive.models.Product;
import com.tutorial.springbootreactive.services.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Controller
public class ProductController {

    private static final Logger LOG = LoggerFactory.getLogger(ProductController.class);

    @Autowired
    private ProductService productService;

    @GetMapping("/product-list")
    public Mono<String> listProducts(Model model){
        Flux<Product> productsFlux =  productService.findAll();
        model.addAttribute("products", productsFlux);
        model.addAttribute("title", "Products List");
        return Mono.just("list");
    }

    @GetMapping("/product-form")
    public Mono<String> createProduct(Model model){
        model.addAttribute("product", new Product());
        model.addAttribute("title", "Product Form");
        return Mono.just("form");
    }

    @PostMapping("/product-form")
    public Mono<String> saveProduct(Product product){
        return productService.saveProduct(product)
                .doOnNext(p -> LOG.info("Product saved: " + p.getName() + " Id: " + p.getId()))
                .thenReturn("redirect:/list");
    }
}
