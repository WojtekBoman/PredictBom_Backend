package com.example.PredictBom.Services.HelperInterfaces;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

public interface MarketsFinder {
    ResponseEntity<?> findMarkets(String marketTitle, String[] marketCategory, Pageable pageable, String sortAttribute, String sortDirection);
}
