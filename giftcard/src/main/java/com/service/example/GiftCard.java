package com.service.example;
/**
 * simple class for creating a record of a gift card
 */
import org.springframework.data.annotation.Id;

public record GiftCard(@Id Long id, Double amount, String owner) {
}
