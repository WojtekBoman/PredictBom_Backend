package com.example.PredictBom.Repositories;

import com.example.PredictBom.Entities.Contract;
import com.example.PredictBom.Entities.SalesOffer;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

public interface ContractRepository extends MongoRepository<Contract,String>, ContractRepositoryCustom {
    Contract findById(int id);
    List<Contract> deleteByBetId(int id);
    List<Contract> findAllByBetIdAndContractOptionAndOffersIsNotNull(int betId, boolean contractOption);
}
