package com.tutorial.springbootreactive.controllers;

import com.tutorial.springbootreactive.models.Category;
import com.tutorial.springbootreactive.services.CategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.support.SessionStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.util.Date;

@Controller
public class CategoryController {

    private static final Logger LOG = LoggerFactory.getLogger(CategoryController.class);

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/category-form")
    public Mono<String> createCategory(Model model){
        model.addAttribute("category", new Category());
        model.addAttribute("title", "Create Category");
        model.addAttribute("button", "Create");
        return Mono.just("category-form");
    }

    @GetMapping("/category-form/{id}")
    public Mono<String> editCategory(@PathVariable String id, Model model){
        return categoryService.findCategoryById(id)
                .doOnNext(category -> {
                    model.addAttribute("category", category);
                    model.addAttribute("title", "Edit Category");
                    model.addAttribute("button", "Edit");
                })
                .defaultIfEmpty(new Category())
                .flatMap(category -> {
                    if(category.getId() == null){
                        return Mono.error(new InterruptedException("The category don't exist."));
                    }
                    return Mono.just(category);
                })
                .then(Mono.just("category-form"))
                .onErrorResume(ex -> Mono.just("redirect:/category-list?error=The+category+do+not+exist"));
    }

    @PostMapping("/category-form")
    public Mono<String> saveCategory(@Valid Category category, BindingResult bindingResult,
                                    Model model, SessionStatus sessionStatus){

        if(bindingResult.hasErrors()){
            model.addAttribute("title", "Error in category form.");
            model.addAttribute("button", "Save");
            return Mono.just("category-form");
        }
        sessionStatus.setComplete();
        return categoryService.saveCategory(category)
                .doOnNext(p -> LOG.info("Category saved: " + p.getName() + " Id: " + p.getId()))
                .thenReturn("redirect:/category-list?success=Category+saved+successfully");
    }

    @GetMapping("/delete-category/{id}")
    public Mono<String> deleteCategory( @PathVariable String id, Model model){
        return categoryService.findCategoryById(id)
                .defaultIfEmpty(new Category())
                .flatMap(category -> {
                    if(category.getId() == null){
                        return Mono.error(new InterruptedException("The category to delete don't exist."));
                    }
                    return Mono.just(category);
                })
                .flatMap(category -> {
                    LOG.info("Deleting category " + category.getName() + " Id: " + category.getId());
                    return categoryService.deleteCategory(category);
                }).then(Mono.just("redirect:/category-list?success=Category+deleted+successfully"))
                .onErrorResume(ex -> Mono.just("redirect:/category-list?error=The+category+to+delete+do+not+exist"));
    }

    @GetMapping("/category-list")
    public Mono<String> listCategories(Model model){
        Flux<Category> categoriesFlux =  categoryService.findAllCategories();
        model.addAttribute("categories", categoriesFlux);
        model.addAttribute("title", "Categories List");
        return Mono.just("category-list");
    }
}
