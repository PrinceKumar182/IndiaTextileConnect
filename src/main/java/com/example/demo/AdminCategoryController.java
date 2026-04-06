package com.example.demo;

import com.example.demo.dto.CategoryDto;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminCategoryController {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @PostMapping("/addCategory")
    public String addCategory(@Valid CategoryDto categoryDto, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/admin";
        }
        try {
            Category category = new Category(categoryDto.getName(), categoryDto.getDescription());
            categoryRepository.save(category);
            redirectAttributes.addFlashAttribute("success", "Category added successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to add category.");
        }
        return "redirect:/admin";
    }

    @PostMapping("/deleteCategory")
    public String deleteCategory(@RequestParam String id, RedirectAttributes redirectAttributes) {
        try {
            categoryRepository.deleteById(id);
            // DO NOT cascading delete products, instead mark them as uncategorized or let them be null
            // The DatabaseMigrationRunner will pick this up on restart, or we can handle it here:
            redirectAttributes.addFlashAttribute("success", "Category deleted.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete category.");
        }
        return "redirect:/admin";
    }
}
