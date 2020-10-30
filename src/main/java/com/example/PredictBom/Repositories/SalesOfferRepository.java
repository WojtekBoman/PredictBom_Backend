package com.example.PredictBom.Repositories;

import com.example.PredictBom.Entities.SalesOffer;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface SalesOfferRepository extends MongoRepository<SalesOffer,String>,SalesOfferRepositoryCustom {

    Optional<SalesOffer> findById(int id);
    List<SalesOffer> deleteByContractId(int contractId);
}
