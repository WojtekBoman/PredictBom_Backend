package com.example.PredictBom.Repositories;

import com.example.PredictBom.Entities.Moderator;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ModeratorRepository extends MongoRepository<Moderator,String> {


}
