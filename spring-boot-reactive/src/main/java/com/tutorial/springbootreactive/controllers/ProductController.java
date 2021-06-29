package com.tutorial.springbootreactive.controllers;

import com.tutorial.springbootreactive.models.Product;
import com.tutorial.springbootreactive.services.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.util.Date;

@SessionAttributes("product")
@Controller
public class ProductController {

    private static final Logger LOG = LoggerFactory.getLogger(ProductController.class);

    @Autowired
    private ProductService productService;

    @GetMapping("/product-form")
    public Mono<String> createProduct(Model model){
        model.addAttribute("product", new Product());
        model.addAttribute("title", "Create Product");
        model.addAttribute("button", "Create");
        return Mono.just("product-form");
    }

    @GetMapping("/product-form/{id}")
    public Mono<String> editProduct(@PathVariable String id, Model model){
        Mono<Product> productMono =  productService.findById(id).defaultIfEmpty(new Product());
        model.addAttribute("product", productMono);
        model.addAttribute("title", "Edit Product");
        model.addAttribute("button", "Edit");
        return Mono.just("product-form");
    }

    @GetMapping("/product-form-v2/{id}")
    public Mono<String> editProductV2(@PathVariable String id, Model model){
        return productService.findById(id)
                .doOnNext(product -> {
                    model.addAttribute("product", product);
                    model.addAttribute("title", "Edit Product");
                    model.addAttribute("button", "Edit");
                })
                .defaultIfEmpty(new Product())
                .flatMap(product -> {
                    if(product.getId() == null){
                        return Mono.error(new InterruptedException("The product don't exist."));
                    }
                    return Mono.just(product);
                })
                .then(Mono.just("product-form"))
                .onErrorResume(ex -> Mono.just("redirect:/product-list?error=The+product+do+not+exist"));
    }

    @PostMapping("/product-form")
    public Mono<String> saveProduct(@Valid Product product, BindingResult bindingResult,
                                    Model model, SessionStatus sessionStatus){

        if(bindingResult.hasErrors()){
            model.addAttribute("title", "Error in product form.");
            model.addAttribute("button", "Save");
            return Mono.just("product-form");
        }
        if(product.getCreatedAt() == null) product.setCreatedAt(new Date());
        sessionStatus.setComplete();
        return productService.saveProduct(product)
                .doOnNext(p -> LOG.info("Product saved: " + p.getName() + " Id: " + p.getId()))
                .thenReturn("redirect:/product-list?success=Product+saved+successfully");
    }

        @GetMapping("/delete-product/{id}")
    public Mono<String> deleteProduct( @PathVariable String id, Model model){
        return productService.findById(id)
                .defaultIfEmpty(new Product())
                .flatMap(product -> {
                    if(product.getId() == null){
                        return Mono.error(new InterruptedException("The product to delete don't exist."));
                    }
                    return Mono.just(product);
                })
                .flatMap(product -> {
                    LOG.info("Deleting product " + product.getName() + " Id: " + product.getId());
                    return productService.deleteProduct(product);
                }).then(Mono.just("redirect:/product-list?success=Product+deleted+successfully"))
                .onErrorResume(ex -> Mono.just("redirect:/product-list?error=The+product+to+delete+do+not+exist"));
    }

    @GetMapping("/product-list")
    public Mono<String> listProducts(Model model){
        Flux<Product> productsFlux =  productService.findAll();
        model.addAttribute("products", productsFlux);
        model.addAttribute("title", "Products List");
        return Mono.just("product-list");
    }
}
