package com.service.example;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface GiftCardRepository extends CrudRepository<GiftCard, Long>, PagingAndSortingRepository<GiftCard, Long> {
    GiftCard findByIdAndOwner(Long id, String owner);

    Page<GiftCard> findByOwner(String owner, PageRequest amount);
}
