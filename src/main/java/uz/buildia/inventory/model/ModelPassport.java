package uz.buildia.inventory.model;

import lombok.Builder;

@Builder
public record ModelPassport(String modelName, int quantity, String color, String size) { }
