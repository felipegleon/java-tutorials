package com.tutorial.springwebfluxresfull.restcontrollers;

import com.tutorial.springwebfluxresfull.models.Category;
import com.tutorial.springwebfluxresfull.models.Product;
import com.tutorial.springwebfluxresfull.services.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public Mono<ResponseEntity<Flux<Category>>> listAllCategories(){
        return Mono.just(
                ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(categoryService.findAllCategories())
        );
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Category>> getCategoryById(@PathVariable String id){
        return categoryService.findCategoryById(id).map(category -> ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(category)
        ).defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Mono<ResponseEntity<Map<String, Object>>> createCategory(@Valid @RequestBody Mono<Category> categoryMono){
        Map<String, Object> response = new HashMap<>();
        return categoryMono.flatMap(category ->
            categoryService.saveCategory(category).map(c -> {
                response.put("category", c);
                response.put("message", "The category was created successful.");
                response.put("timestamp", new Date());
                return ResponseEntity
                        .created(URI.create("/api/categories/".concat(c.getId())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(response);
            })
        ).onErrorResume(throwable -> Mono.just(throwable)
                .cast(WebExchangeBindException.class)
                .flatMap(e -> Mono.just(e.getFieldErrors()))
                .flatMapMany(Flux::fromIterable)
                .map(fieldError -> "The field "+ fieldError.getField() + " " + fieldError.getDefaultMessage())
                .collectList()
                .flatMap(errorsList -> {
                    response.put("errors", errorsList);
                    response.put("timestamp", new Date());
                    response.put("status", HttpStatus.BAD_REQUEST.value());
                    return Mono.just(ResponseEntity.badRequest().body(response));
                })
        );
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<Category>> editCategory (@PathVariable String id, @RequestBody Category category){
        return categoryService.findCategoryById(id).flatMap(c ->{
            c.setName(category.getName());
            return categoryService.saveCategory(c);
        }).map(p -> ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(p)
        ).defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteCategory (@PathVariable String id){
        return categoryService.findCategoryById(id).flatMap(c ->
                categoryService.deleteCategory(c)
                    .then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)))
        ).defaultIfEmpty(new ResponseEntity<Void>(HttpStatus.NOT_FOUND));
    }
}
