package com.project.demo.rest.product;

import com.project.demo.logic.entity.category.Category;
import com.project.demo.logic.entity.category.CategoryRepository;
import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.http.HttpResponse;
import com.project.demo.logic.entity.http.Meta;
import com.project.demo.logic.entity.product.Product;
import com.project.demo.logic.entity.product.ProductRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.hibernate.engine.spi.Resolution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/products")
public class ProductRestController {

    //Permite la inyeccióna automática de dependencias
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CategoryRepository categoryRepository;

    // Listar los productos
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'USER')")
    public ResponseEntity<?> getAllProducts(@RequestParam (defaultValue = "1") int page,
                                            @RequestParam (defaultValue = "10") int size,
                                            HttpServletRequest request) {

        Pageable pageable = PageRequest.of(page-1, size);
        Page<Product> productPage = productRepository.findAll(pageable);
        Meta meta = new Meta(request.getMethod(), request.getRequestURL().toString());
        meta.setTotalPages(productPage.getTotalPages());
        meta.setTotalElements(productPage.getTotalElements());
        meta.setPageNumber(productPage.getNumber() + 1);
        meta.setPageSize(productPage.getSize());

        return new GlobalResponseHandler().handleResponse("Products loaded successfully", productPage.getContent(), HttpStatus.OK, meta);

    }

    // Crear los productos
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    public ResponseEntity<?> createProduct(@RequestBody Product product, HttpServletRequest request) {
        // Se busca la categoría por medio del id que se coloca en el JSON si no se encuentra el id
        Optional<Category> optionalCategory = categoryRepository.findById(product.getCategory().getId());
        if(optionalCategory.isEmpty()){
            return new GlobalResponseHandler().handleResponse("Category id " + product.getCategory().getId() + " not found", HttpStatus.BAD_REQUEST, request);
        }
        product.setCategory(optionalCategory.get());
        Product savedProduct = productRepository.save(product);
        return new GlobalResponseHandler().handleResponse("Product created successfully", savedProduct, HttpStatus.CREATED, request);
    }




    // Actualizar los productos
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    public ResponseEntity<?> updateProduct(@PathVariable long id, @RequestBody Product product, HttpServletRequest request) {
        Optional<Category> optionalCategory = categoryRepository.findById(product.getCategory().getId());
        Optional<Product> optionalProduct = productRepository.findById(id);
        if(optionalProduct.isPresent()){
            if(optionalCategory.isEmpty()){
                // En el caso de que la categoría que se colocó no exista:
                return new GlobalResponseHandler().handleResponse("Category id " + product.getCategory().getId() + " not found",
                        HttpStatus.NOT_FOUND, request);
            }

            // Asignar los valores del producto a actualizar:
            product.setId(id);
            product.setCategory(optionalCategory.get());
            // Asignar el producto actualizado a la BD:
            productRepository.save(product);

            // En el caso de que todo esté correcto, que devuelva los datos
            return new GlobalResponseHandler().handleResponse("Product updated successfully",
                    product, HttpStatus.OK, request);

        }

        // En el caso de que no exista el id de producto indicado:
        return new GlobalResponseHandler().handleResponse("Product id " + id + " not found",
                HttpStatus.NOT_FOUND, request);
    }

    // Eliminar los productos
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    public ResponseEntity<?>  deleteProduct(@PathVariable long id, HttpServletRequest request) {
        Optional<Product> optionalProduct = productRepository.findById(id);

        // Si encuentra un producto para eliminar:
        if(optionalProduct.isPresent()){
            productRepository.deleteById(id);
            return new GlobalResponseHandler().handleResponse("Product deleted successfully", HttpStatus.OK, request);
        }

        // En el caso de que no:
        return new GlobalResponseHandler().handleResponse("Product id " + id + " not found",
                HttpStatus.NOT_FOUND, request);
    }
}
