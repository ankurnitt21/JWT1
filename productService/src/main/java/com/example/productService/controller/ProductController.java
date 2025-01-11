package com.example.productService.controller;

import com.example.productService.dto.ProductDTO;
import com.example.productService.entity.Order;
import com.example.productService.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/products")
public class ProductController {
    @Autowired
    private ProductService productService;

    @GetMapping
    public String getAllProducts(Model model) {
        List<ProductDTO> products = productService.getAllProducts();
        model.addAttribute("products", products);
        return "product-list";
    }

    @GetMapping("/{id}")
    public String getProductById(@PathVariable Long id, Model model) {
        ProductDTO product = productService.getProductById(id);
        model.addAttribute("product", product);
        return "product-detail";
    }

    @GetMapping("/new")
    public String createProductForm(Model model) {
        model.addAttribute("product", new ProductDTO());
        return "product-form";
    }

    @PostMapping
    public String createProduct(@ModelAttribute ProductDTO productDTO) {
        productService.createProduct(productDTO);
        return "redirect:/products";
    }

    @GetMapping("/edit/{id}")
    public String updateProductForm(@PathVariable Long id, Model model) {
        ProductDTO product = productService.getProductById(id);
        model.addAttribute("product", product);
        return "product-form";
    }

    @PostMapping("/{id}")
    public String updateProduct(@PathVariable Long id, @ModelAttribute ProductDTO productDTO) {
        productService.updateProduct(id, productDTO);
        return "redirect:/products";
    }

    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return "redirect:/products";
    }

    @PostMapping("/order_placed")
    @ResponseBody
    public String orderPlaced(@RequestParam String userId,
                              @RequestParam List<String> productIds,
                              @RequestParam double totalAmount,
                              @RequestParam String status,
                              @RequestParam LocalDateTime createdAt) {
        Order orderDTO = new Order(userId, productIds, totalAmount, status, createdAt);
        WebClient webClient = WebClient.create("http://localhost:8082");
        ResponseEntity<String> responseEntity = webClient.post()
                .uri("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(orderDTO))
                .exchangeToMono(clientResponse -> clientResponse.toEntity(String.class))
                .block();
        return "order Placed";
    }
}