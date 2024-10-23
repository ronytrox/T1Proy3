package com.project.demo.rest.category;

import com.project.demo.logic.entity.category.Category;
import com.project.demo.logic.entity.category.CategoryRepository;
import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.http.Meta;
import com.project.demo.logic.entity.product.Product;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/categories")
public class CategoryRestController {

    //Permite la inyeccióna automática de dependencias
    @Autowired
    private CategoryRepository categoryRepository;

    // Listar las categorías
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'USER')")
    public ResponseEntity<?> getAllCategories(@RequestParam (defaultValue = "1") int page,
                                              @RequestParam (defaultValue = "10") int size,
                                              HttpServletRequest request) {
    
        Pageable pageable = PageRequest.of(page-1, size);
        Page<Category> categoryPage = categoryRepository.findAll(pageable);
        Meta meta = new Meta(request.getMethod(), request.getRequestURL().toString());
        meta.setTotalPages(categoryPage.getTotalPages());
        meta.setTotalElements(categoryPage.getTotalElements());
        meta.setPageNumber(categoryPage.getNumber() + 1);
        meta.setPageSize(categoryPage.getSize());

        return new GlobalResponseHandler().handleResponse("Categories loaded successfully", categoryPage.getContent(), HttpStatus.OK, meta);
    }

    // Crear las categorías
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    public ResponseEntity<?> createCategory(@RequestBody Category category, HttpServletRequest request) {
        Category savedCategory = categoryRepository.save(category);
        return new GlobalResponseHandler().handleResponse("Category created successfully", savedCategory, HttpStatus.CREATED, request);
    }

    // Actualizar las categorías
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    public ResponseEntity<?> updateCategory(@PathVariable long id, @RequestBody Category category, HttpServletRequest request) {
        Optional<Category> optionalCategory = categoryRepository.findById(id);
        if(optionalCategory.isPresent()){

            // Asignar los valores de la categoría a actualizar:
            category.setId(id);
            // Asignar la categoría actualizada a la BD:
            categoryRepository.save(category);

            // En el caso de que todo esté correcto, que devuelva los datos
            return new GlobalResponseHandler().handleResponse("Category updated successfully",
                    category, HttpStatus.OK, request);

        }

        // En el caso de que no exista el id de categoría indicado:
        return new GlobalResponseHandler().handleResponse("Category id " + id + " not found",
                HttpStatus.NOT_FOUND, request);
    }

    // Eliminar las categorías
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    public ResponseEntity<?> deleteCategory(@PathVariable long id, HttpServletRequest request) {
        Optional<Category> optionalCategory = categoryRepository.findById(id);

        // Si encuentra un categoryo para eliminar:
        if(optionalCategory.isPresent()){
            categoryRepository.deleteById(id);
            return new GlobalResponseHandler().handleResponse("Category deleted successfully", HttpStatus.OK, request);
        }

        // En el caso de que no:
        return new GlobalResponseHandler().handleResponse("Category id " + id + " not found",
                HttpStatus.NOT_FOUND, request);
    }


}
