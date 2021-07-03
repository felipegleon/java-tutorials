package com.tutorial.springwebfluxresfull.handler;

import com.tutorial.springwebfluxresfull.models.Category;
import com.tutorial.springwebfluxresfull.models.Product;
import com.tutorial.springwebfluxresfull.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.net.URI;
import java.util.Date;
import java.util.UUID;

@Component
public class ProductHandler {

    @Value("${config.uploads.path}")
    private String path;

    @Autowired
    private ProductService productService;

    @Autowired
    private Validator validator;

    public Mono<ServerResponse> getAllProducts (ServerRequest serverRequest){
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(productService.findAll(), Product.class);
    }

    public Mono<ServerResponse> getProductById (ServerRequest serverRequest){
        String id =  serverRequest.pathVariable("id");
        return productService.findById(id).flatMap(product ->
            ServerResponse.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(product))
                    .switchIfEmpty(ServerResponse.notFound().build())
        );
    }

    public Mono<ServerResponse> createProduct (ServerRequest serverRequest){
        Mono<Product> productMono = serverRequest.bodyToMono(Product.class);
        return productMono.flatMap(product ->{
            Errors errors = new BeanPropertyBindingResult(product, Product.class.getName());
            validator.validate(product, errors);
            if(errors.hasErrors()){
                return Flux.fromIterable(errors.getFieldErrors())
                        .map(fieldError -> "The field " + fieldError.getField() + " " + fieldError.getDefaultMessage())
                        .collectList()
                        .flatMap(errorsList -> ServerResponse.badRequest().body(BodyInserters.fromValue(errorsList)));
            }
            if(product.getCreatedAt() == null) product.setCreatedAt(new Date());
             return productService.saveProduct(product).flatMap(p ->
                     ServerResponse.created(URI.create("/handler/api/products/".concat(p.getId())))
                             .contentType(MediaType.APPLICATION_JSON)
                             .body(BodyInserters.fromValue(p))
             );
        });
    }

    public Mono<ServerResponse> uploadImage (ServerRequest serverRequest){
        String id =  serverRequest.pathVariable("id");
        return serverRequest.multipartData().map(multiPart -> multiPart.toSingleValueMap().get("file"))
                .cast(FilePart.class)
                .flatMap(file -> productService.findById(id)
                        .flatMap( product -> {
                            product.setImage(UUID.randomUUID().toString() + file.filename()
                                    .replace(" ", "")
                                    .replace(":", "")
                                    .replace("\\", ""));
                            return file.transferTo(new File(path, product.getImage()))
                                    .then(productService.saveProduct(product));
                        })
                ).flatMap(product -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(product))
                ).switchIfEmpty(ServerResponse.noContent().build());
    }

    public Mono<ServerResponse> createProductWithImage(ServerRequest serverRequest) {
        Mono<Product> productMono =  serverRequest.multipartData().map(multiPart -> {
            FormFieldPart name = (FormFieldPart) multiPart.toSingleValueMap().get("name");
            FormFieldPart price = (FormFieldPart) multiPart.toSingleValueMap().get("price");
            FormFieldPart categoryId = (FormFieldPart) multiPart.toSingleValueMap().get("category.id");
            FormFieldPart categoryName = (FormFieldPart) multiPart.toSingleValueMap().get("category.name");

            Category category =  new Category(categoryId.value(), categoryName.value());
            return new Product(null, name.value(), Double.parseDouble(price.value()), null, category, null);
        });
        return serverRequest.multipartData().map(multiPart -> multiPart.toSingleValueMap().get("file"))
                .cast(FilePart.class)
                .flatMap(file -> productMono
                        .flatMap( product -> {
                            product.setImage(UUID.randomUUID().toString() + file.filename()
                                    .replace(" ", "")
                                    .replace(":", "")
                                    .replace("\\", ""));
                            product.setCreatedAt(new Date());
                            return file.transferTo(new File(path, product.getImage()))
                                    .then(productService.saveProduct(product));
                        })
                ).flatMap(product -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(product))
                );
    }

    public Mono<ServerResponse> editProduct(ServerRequest serverRequest) {
        Mono<Product> productMono = serverRequest.bodyToMono(Product.class);
        String id =  serverRequest.pathVariable("id");
        Mono<Product> productMonoDb = productService.findById(id);

        return productMonoDb.zipWith(productMono, (productDb, product) -> {
            productDb.setName(product.getName());
            productDb.setPrice(product.getPrice());
            productDb.setCategory(product.getCategory());
            return productDb;
        }).flatMap(p -> ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(productService.saveProduct(p), Product.class)
        ).switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> deleteProduct(ServerRequest serverRequest) {
        String id =  serverRequest.pathVariable("id");
        Mono<Product> productMonoDb = productService.findById(id);
        return productMonoDb.flatMap(product -> productService.deleteProduct(product)
                .then(ServerResponse.noContent().build())
        ).switchIfEmpty(ServerResponse.notFound().build());
    }


}
