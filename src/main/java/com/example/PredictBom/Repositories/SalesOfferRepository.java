package com.example.PredictBom.Repositories;

import com.example.PredictBom.Entities.Offer;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface SalesOfferRepository extends MongoRepository<Offer,String>,SalesOfferRepositoryCustom {

    Optional<Offer> findById(int id);
    void deleteByContractId(int contractId);
    void deleteById(int id);
}
