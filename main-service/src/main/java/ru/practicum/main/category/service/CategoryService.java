package ru.practicum.main.category.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.main.category.dto.CategoryDto;
import ru.practicum.main.category.dto.NewCategoryDto;
import ru.practicum.main.category.model.Category;

import java.util.List;

public interface CategoryService {

    CategoryDto createCategoryViaAdmin(NewCategoryDto newCategoryDto);

    void deleteCategoryViaAdminById(Long catId);

    CategoryDto patchCategoryViaAdmin(CategoryDto categoryDto, Long catId);

    Category getCategoryById(Long catId);

    List<CategoryDto> getAllCategoryViaPublic(Pageable page);

    CategoryDto getCategoryByIdViaPublic(Long catId);

}
