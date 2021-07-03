package com.tutorial.springwebfluxresfull.config;

import com.tutorial.springwebfluxresfull.handler.ProductHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
public class RouterFunctionConfig {


    @Bean
    public RouterFunction<ServerResponse> routes(ProductHandler productHandler){
        return RouterFunctions.route(GET("/handler/api/products").or(GET("/handler/api/v2 /products")), productHandler::getAllProducts)
                .andRoute(GET("/handler/api/products/{id}"), productHandler::getProductById)
                .andRoute(POST("/handler/api/products"), productHandler::createProduct)
                .andRoute(PUT("/handler/api/products/{id}"), productHandler::editProduct)
                .andRoute(DELETE("/handler/api/products/{id}"), productHandler::deleteProduct)
                .andRoute(POST("/handler/api/products/upload/image/{id}"), productHandler::uploadImage)
                .andRoute(POST("/handler/api/products/save-product-with-image"), productHandler::createProductWithImage);
    }
}
