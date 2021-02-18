package com.example.PredictBom.Repositories;

import com.example.PredictBom.Entities.Contract;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ContractRepository extends MongoRepository<Contract,String>, ContractRepositoryCustom {

    Optional<Contract> findById(int id);

    Optional<Contract> findByIdAndPlayerId(int id, String playerId);

    void deleteById(int id);

    List<Contract> deleteByBetId(int id);

    @Query("{'bet.marketId': ?0}")
    List<Contract> findAllByMarketId(int id);

    @Query("{'bet.id': ?0, 'playerId' : {$ne : null}}")
    List<Contract> findAllByBetIdAndPlayerIdIsNotNull(int betId);

    @Query("{'playerId': ?0,'contractStatus': {$regex : ?1, $options: 'i'}, 'contractOption': ?2, 'bet.title':{$regex : ?3, $options: 'i'},'marketInfo.topic':{$regex : ?4, $options: 'i'},'marketInfo.marketCategory':{$in : ?5}}")
    Page<Contract> findByPlayerIdAndContractOption(String playerId, String contractStatus, boolean ContractOption,String betTitle, String topic,List<String> marketCategory, Pageable pageable);

    @Query("{'playerId': ?0, 'contractStatus': {$regex : ?1, $options: 'i'}, 'contractOption': ?2, 'bet.title':{$regex : ?3, $options: 'i'},'marketInfo.topic':{$regex : ?4, $options: 'i'}}")
    Page<Contract> findByPlayerIdAndContractOption(String playerId,String contractStatus, boolean ContractOption, String betTitle, String topic, Pageable pageable);

    Optional<Contract> findByPlayerIdAndBetIdAndContractOption(String playerId,int betId, boolean contractOption);

    @Query("{'bet.id': ?0,'contractOption': ?1}")
    Optional<Contract> findByBetIdAndContractOption(int betId, boolean contractOption);

    @Query("{'playerId': ?0,'contractStatus': {$regex : ?1,$options: 'i'}, 'bet.title':{$regex : ?2,$options: 'i'},'marketInfo.topic':{$regex : ?3,$options: 'i'},'marketInfo.marketCategory':{$in : ?4}}")
    Page<Contract> findContractsByPlayerId(String playerId,String contractStatus,String betTitle, String topic,List<String> marketCategory, Pageable pageable);

    @Query("{'playerId': ?0,'contractStatus': {$regex : ?1,$options: 'i'}, 'bet.title':{$regex : ?2,$options: 'i'},'marketInfo.topic':{$regex : ?3,$options: 'i'}}")
    Page<Contract> findContractsByPlayerId(String playerId,String contractStatus,String betTitle, String topic, Pageable pageable);

    @Query("{'bet.marketId': ?0, 'playerId':{$eq : null}}")
    List<Contract> findAllByMarketIdAndPlayerIdIsNull(int marketId);
    @Query("{'bet.id': ?0,'contractOption': ?1,'playerId' : {$ne : ?2},'offers' : {$ne : null}}")
    List<Contract> findOffersToBuy(int betId, boolean contractOption, String username);

    @Query("{'bet.id': ?0,'contractOption': ?1,'offers' : {$ne : null}}")
    List<Contract> findOffersToBuy(int betId, boolean contractOption);
}
