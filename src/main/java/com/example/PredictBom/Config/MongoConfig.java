package com.example.PredictBom.Config;


import com.mongodb.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.apache.commons.codec.binary.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.retry.annotation.EnableRetry;


@Configuration
@EnableRetry
//@EnableMongoRepositories(basePackages = "com.example.PredictBom")
public class MongoConfig extends AbstractMongoClientConfiguration {

    @Bean
    MongoTransactionManager transactionManager(MongoDatabaseFactory dbFactory) {
        return new MongoTransactionManager(dbFactory);
    }

    @Override
    @Bean
    public MongoClient mongoClient() {

        return MongoClients.create(
                MongoClientSettings.builder()
                        .applyConnectionString(new ConnectionString("mongodb+srv://admin_predict_bom:cPe9wwP19sigtPeu@cluster0.yjasx.mongodb.net/predict_bom?retryWrites=true&w=majority"))
                        .applicationName("predict_bom")
                        .readConcern(ReadConcern.MAJORITY)
                        .writeConcern(WriteConcern.ACKNOWLEDGED)
                        .readPreference(ReadPreference.primary())
                        .build());
    }

    @Override
    protected String getDatabaseName() {
        return "predict_bom";
    }

//    @Override
//    protected String getDatabaseName() {
//        return "predict_bom";
//    }
//
//    @Override
//    public MongoClient mongoClient() {
//        final ConnectionString connectionString = new ConnectionString("mongodb://localhost:27017/predict_bom");
//        final MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
//                .applyConnectionString(connectionString)
//                .build();
//        return MongoClients.create(mongoClientSettings);
//    }
}