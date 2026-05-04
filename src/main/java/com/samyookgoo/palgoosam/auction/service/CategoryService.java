package com.samyookgoo.palgoosam.auction.service;

import com.samyookgoo.palgoosam.auction.domain.Category;
import com.samyookgoo.palgoosam.auction.dto.response.CategoryResponseDto;
import com.samyookgoo.palgoosam.auction.repository.CategoryRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryResponseDto> getCategory() {
        List<Category> categoryList = categoryRepository.findAllWithChildrenAndParent();
        return categoryList.stream().map(category -> {
            CategoryResponseDto responseDto = new CategoryResponseDto();
            responseDto.setId(category.getId());
            responseDto.setName(category.getName());
            responseDto.setChildrenIdList(
                    category.getChildren().stream().map(Category::getId).collect(Collectors.toList()));
            responseDto.setParentId(category.getParent() != null ? category.getParent().getId() : null);
            return responseDto;
        }).toList();
    }

    public List<Long> collectAncestorIds(Category category) {
        List<Long> ids = new ArrayList<>();
        Category current = category;
        while (current != null) {
            ids.add(current.getId());
            current = current.getParent();
        }
        return ids;
    }
}
