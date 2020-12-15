package com.example.PredictBom.Repositories;

import com.example.PredictBom.Entities.Contract;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ContractRepository extends MongoRepository<Contract,String>, ContractRepositoryCustom {
    Optional<Contract> findById(int id);
    Optional<Contract> findByIdAndPlayerId(int id, String playerId);
    Contract deleteById(int id);
    List<Contract> deleteByBetId(int id);
    @Query("{'bet.marketId': ?0}")
    List<Contract> findAllByMarketId(int id);
    @Query("{'bet.id': ?0,'contractOption': ?1,'playerId' : {$ne : null}}")
    List<Contract> findAllByBetIdAndContractOptionAndPlayerIdIsNotNull(int betId, boolean contractOption);
    @Query("{'bet.id': ?0,'contractOption': ?1,'offers' : {$ne : null}}")
    List<Contract> findAllByBetIdAndContractOptionAndOffersIsNotNull(int betId, boolean contractOption);
    @Query("{'bet.id': ?0, 'playerId' : {$ne : null}}")
    List<Contract> findAllByBetIdAndPlayerIdIsNotNull(int betId);
    List<Contract> findByPlayerId(String playerId, Sort sort);
    List<Contract> findByPlayerIdAndContractOption(String playerId, boolean ContractOption, Sort sort);
    List<Contract> findByPlayerIdOrderByModifiedDateDesc(String playerId);
    Optional<Contract> findByPlayerIdAndBetIdAndContractOption(String playerId,int betId, boolean contractOption);
    @Query("{'bet.id': ?0,'contractOption': ?1}")
    Optional<Contract> findByBetIdAndContractOption(int betId, boolean contractOption);
    @Query("{'playerId': ?0}")
    List<Contract> findNotSolvedMarketsByPlayerId(String playerId,Sort sort);
    List<Contract> deleteAllByPlayerIdIsNull();
    @Query("{'bet.marketId': ?0, 'playerId':{$eq : null}}")
    List<Contract> findAllByMarketIdAndPlayerIdIsNull(int marketId);
    @Query("{'bet.id': ?0,'contractOption': ?1,'playerId' : {$ne : ?2},'offers' : {$ne : null}}")
    List<Contract> findOffersToBuy(int betId, boolean contractOption, String username);

    @Query("{'bet.id': ?0,'contractOption': ?1,'offers' : {$ne : null}}")
    List<Contract> findOffersToBuy(int betId, boolean contractOption);
}
