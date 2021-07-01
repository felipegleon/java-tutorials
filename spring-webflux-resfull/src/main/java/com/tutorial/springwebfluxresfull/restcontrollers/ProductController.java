package com.tutorial.springwebfluxresfull.restcontrollers;

import com.tutorial.springwebfluxresfull.models.Product;
import com.tutorial.springwebfluxresfull.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.io.File;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Value("${config.uploads.path}")
    private String path;

    @Autowired
    private ProductService productService;

    @GetMapping
    public Mono<ResponseEntity<Flux<Product>>> listAllProducts(){
        return Mono.just(
                ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(productService.findAll())
        );
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Product>> getProductById(@PathVariable String id){
        return productService.findById(id).map(product -> ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(product)
        ).defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Mono<ResponseEntity<Map<String, Object>>> createProduct( @Valid @RequestBody Mono<Product> productMono){
        Map<String, Object> response = new HashMap<>();
        return productMono.flatMap(product -> {
            if(product.getCreatedAt() == null) product.setCreatedAt(new Date());
            return productService.saveProduct(product).map(p -> {
                response.put("product", p);
                response.put("message", "The product was created successful.");
                response.put("timestamp", new Date());
                return ResponseEntity
                        .created(URI.create("/api/products/".concat(p.getId())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(response);
            });
        }).onErrorResume(throwable -> Mono.just(throwable)
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

    @PostMapping("/upload/image/{id}")
    public Mono<ResponseEntity<Product>> uploadImage(@PathVariable String id, @RequestPart FilePart file){
        return productService.findById(id).flatMap(p ->{
            p.setImage(UUID.randomUUID().toString() + "-" + file.filename()
                    .replace(" ", "")
                    .replace(":", "")
                    .replace("\\", ""));
            return file.transferTo(new File(path + p.getImage())).then(productService.saveProduct(p));
        }).map(p -> ResponseEntity.ok(p))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping("/save-product-with-image")
    public Mono<ResponseEntity<Product>> saveProductWithImage(Product product, @RequestPart FilePart file){
        if(product.getCreatedAt() == null) product.setCreatedAt(new Date());
        product.setImage(UUID.randomUUID().toString() + "-" + file.filename()
                .replace(" ", "")
                .replace(":", "")
                .replace("\\", ""));
        return file.transferTo(new File(path + product.getImage())).then(productService.saveProduct(product))
                .map(p -> ResponseEntity
                        .created(URI.create("/api/products/".concat(p.getId())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(p)
                );
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<Product>> editProduct (@PathVariable String id, @RequestBody Product product){
        return productService.findById(id).flatMap(p ->{
            p.setName(product.getName());
            p.setPrice(product.getPrice());
            p.setCategory(product.getCategory());
            return productService.saveProduct(p);
        }).map(p -> ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(p)
        ).defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteProduct (@PathVariable String id){
        return productService.findById(id).flatMap(p -> {
            return productService.deleteProduct(p)
                    .then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)));
        }).defaultIfEmpty(new ResponseEntity<Void>(HttpStatus.NOT_FOUND));
    }
}
