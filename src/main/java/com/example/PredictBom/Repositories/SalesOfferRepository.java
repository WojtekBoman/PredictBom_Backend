package com.example.PredictBom.Repositories;

import com.example.PredictBom.Entities.Offer;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface SalesOfferRepository extends MongoRepository<Offer,String>,SalesOfferRepositoryCustom {

    Optional<Offer> findById(int id);
    List<Offer> deleteByContractId(int contractId);
    Offer deleteById(int id);
}
