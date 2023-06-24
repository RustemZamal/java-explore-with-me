package ru.practicum.main.category.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.category.dto.CategoryDto;
import ru.practicum.main.category.dto.NewCategoryDto;
import ru.practicum.main.category.mapper.CategoryMapper;
import ru.practicum.main.category.model.Category;
import ru.practicum.main.category.repository.CategoryRepository;
import ru.practicum.main.exeption.NotFoundException;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;


    @Transactional
    @Override
    public CategoryDto createCategoryViaAdmin(NewCategoryDto newCategoryDto) {
        return CategoryMapper.toCategoryDto(categoryRepository.save(CategoryMapper.toCategory(newCategoryDto)));
    }

    @Transactional
    @Override
    public void deleteCategoryViaAdminById(Long catId) {
        getCategoryById(catId);
        categoryRepository.deleteById(catId);
    }

    @Transactional
    @Override
    public CategoryDto patchCategoryViaAdmin(CategoryDto categoryDto, Long catId) {
        Category category = categoryRepository.getReferenceById(catId);
        if (categoryDto.getName() != null) {
            category.setName(categoryDto.getName());
        }

        return CategoryMapper.toCategoryDto(categoryRepository.save(category));
    }

    @Override
    public Category getCategoryById(Long catId) {
        return categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException(String.format("Category with id=%d was not found", catId)));
    }

    @Override
    public List<CategoryDto> getAllCategoryViaPublic(Pageable page) {
        return categoryRepository.findAll(page).map(CategoryMapper::toCategoryDto).getContent();
    }

    @Override
    public CategoryDto getCategoryByIdViaPublic(Long catId) {
        return CategoryMapper.toCategoryDto(getCategoryById(catId));
    }
}
