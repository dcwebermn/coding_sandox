package com.service.example;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/giftcards")
public class GiftCardController {
    private GiftCardRepository giftCardRepository;

    public GiftCardController(GiftCardRepository giftCardRepository) {
        this.giftCardRepository = giftCardRepository;
    }

    @GetMapping("/{requestedId}")
    public ResponseEntity<GiftCard> findById(@PathVariable Long requestedId, Principal principal) {
        GiftCard giftCard = findGiftCard(requestedId, principal);
        if (giftCard != null) {
            return ResponseEntity.ok(giftCard);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    private ResponseEntity<Void> createGiftCard(@RequestBody GiftCard newGiftCardRequest, UriComponentsBuilder ucb, Principal principal) {
        GiftCard giftCard = new GiftCard(null, newGiftCardRequest.amount(), principal.getName());
        GiftCard savedGiftCard = giftCardRepository.save(giftCard);
        URI endpointOfNewGiftCard = ucb
                .path("giftcards/{id}")
                .buildAndExpand(savedGiftCard.id())
                .toUri();
        return ResponseEntity.created(endpointOfGiftCard).build();
    }

    @GetMapping
    public ResponseEntity<List<GiftCard>> findAll(Pageable pageable, Principal principal) {
        Page<GiftCard> page = giftCardRepository.findByOwner(principal.getName(),
                PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        pageable.getSortOr(Sort.by(Sort.Direction.ASC, "amount"))
                ));
        return ResponseEntity.ok(page.getContent());
    }

    @PutMapping("/{requestedId}")
    private ResponseEntity<Void> pushGiftCard(@PathVariable Long requestedId,
    @RequestBody GiftCard giftCardUdpate, Principal principal) {
        GiftCard giftCard = findGiftCard(requestedId, principal);
        if (giftCard != null){
          GiftCard updatedGiftCard = new GiftCard(giftCard.id(), giftCardUdpate.amount(), principal.getName());
          giftCardRepository.save(updatedGiftCard);
          return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    private GiftCard findGiftCard(Long requestedId, Principal principal) {
        return giftCardRepository.findByIdAndOwner(requestedId, principal.getName());
    }

    @DeleteMapping("/{id}")
    private ResponseEntity<Void> deleteGiftCard(@PathVariable Long id) {
        giftCardRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
