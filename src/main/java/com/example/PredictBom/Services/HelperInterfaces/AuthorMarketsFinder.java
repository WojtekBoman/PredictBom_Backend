package com.example.PredictBom.Services.HelperInterfaces;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

public interface AuthorMarketsFinder {
    ResponseEntity<?> findMarkets(String username, String marketTitle, String[] marketCategory, Pageable pageable, String sortAttribute, String sortDirection);
}
