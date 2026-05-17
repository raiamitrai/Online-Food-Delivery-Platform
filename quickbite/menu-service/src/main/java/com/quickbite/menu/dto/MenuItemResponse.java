package com.quickbite.menu.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemResponse implements Serializable {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private Boolean isVegetarian;
    private Boolean isAvailable;
    private Long categoryId;
}
