package com.example.PredictBom.Controllers;


import com.example.PredictBom.BetRequest;
import com.example.PredictBom.CreateMarketRequest;
import com.example.PredictBom.Entities.*;
import com.example.PredictBom.Repositories.*;
import com.example.PredictBom.Security.JWT.JwtUtils;
import com.example.PredictBom.Services.CounterService;
import com.example.PredictBom.Services.PasswordResetTokenService;
import com.example.PredictBom.Services.PredictionMarketService;
import com.example.PredictBom.Services.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;
import com.mongodb.client.MongoClients;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.IFeatureAwareVersion;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.distribution.GenericVersion;
import de.flapdoodle.embed.process.runtime.Network;
import jdk.nashorn.internal.ir.RuntimeNode;

import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.*;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;

import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;


@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
class MarketControllerTest {

    public static final MediaType APPLICATION_JSON_UTF8 = new MediaType(MediaType.APPLICATION_JSON.getType(), MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));

    @Autowired
    private MockMvc mvc;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    CounterRepository counterRepository;

//    @Autowired
//    RoleRepository roleRepository;

//    @Autowired
//    PasswordEncoder encoder;
//
//    private static final String CONNECTION_STRING = "mongodb://%s:%d";
//
//    private MongodExecutable mongodExecutable;
//    private MongoTemplate mongoTemplate;
//
//
//    @AfterEach
//    void clean() {
//        mongodExecutable.stop();
//    }
//
//    @BeforeEach
//    void setup() throws Exception {
//        String ip = "localhost";
//        int port = 27017;
//
//        IFeatureAwareVersion version = de.flapdoodle.embed.mongo.distribution.Versions.withFeatures(
//                new GenericVersion("4.0.0"),
//                Version.Main.PRODUCTION.getFeatures());
//
//        IMongodConfig mongodConfig = new MongodConfigBuilder().version(version)
//                .net(new Net(ip, port, Network.localhostIsIPv6()))
//                .build();
//
//        MongodStarter starter = MongodStarter.getDefaultInstance();
//        mongodExecutable = starter.prepare(mongodConfig);
//        mongodExecutable.start();
//        mongoTemplate = new MongoTemplate(MongoClients.create(String.format(CONNECTION_STRING, ip, port)), "predict_bom");
//
//        Role player = new Role(ERole.ROLE_PLAYER);
//        Role mod = new Role(ERole.ROLE_MODERATOR);
//        Role admin = new Role(ERole.ROLE_ADMIN);
////
//        mongoTemplate.save(player,"roles");
//        mongoTemplate.save(mod,"roles");
//        mongoTemplate.save(admin,"roles");
//
//        Player playerUser = new Player("RynkoznawcaWojtek", "Wojtek", "Boman",
//                "boman@gmail.com",
//                encoder.encode("wojtek1234"), 1000);
//
//        Optional<Role> userRole = roleRepository.findByName(ERole.ROLE_MODERATOR);
//
//
//        User moder = new User("moderator", "Wojtek", "Boman",
//                "mod@gmail.com",
//                encoder.encode("moderator123"));
//        Set<Role> set = new HashSet<Role>();
//        set.add(userRole.get());
//        moder.setRoles(set);
//        mongoTemplate.save(playerUser,"users");
//        mongoTemplate.save(moder,"users");
//    }



    @Test
    void getMarkets() throws Exception {

        RequestBuilder request = MockMvcRequestBuilders
                .get("/markets/?marketTitle=&marketCategory=&page=0&size=10&sortAttribute=createdDate&sortDirection=desc")
                .contentType("application/json");
        MvcResult result = mvc.perform(request).andReturn();
        assertEquals(200,result.getResponse().getStatus());

    }

    @Test
    void createMarket_expected200() throws Exception {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken("moderator", "moderator123"));
        String jwt = jwtUtils.generateJwtToken(authentication);
        SecurityContextHolder.getContext().setAuthentication(authentication);
//

        CreateMarketRequest createMarketRequest = CreateMarketRequest
                .builder().description("opis").predictedEndDate("2021-01-01").topic("topic")
                .category("SPORT").build();

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        String requestJson=ow.writeValueAsString(createMarketRequest);
        RequestBuilder request = MockMvcRequestBuilders
                .post("/markets/new")
                .header("Authorization",jwt)
                .contentType(APPLICATION_JSON_UTF8)
                .content(requestJson);

        MvcResult result = mvc.perform(request).andReturn();
        assertEquals(200,result.getResponse().getStatus());
    }

    @Test
    void addBet_expected200() throws Exception {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken("moderator", "moderator123"));
        String jwt = jwtUtils.generateJwtToken(authentication);
        SecurityContextHolder.getContext().setAuthentication(authentication);
//

        Counter counter = counterRepository.findByName("markets");

        BetRequest betRequest = BetRequest.builder().marketId(counter.getValue()).chosenOption("test1").noPrice(0.5).yesPrice(0.5).shares(10000).build();

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        String requestJson=ow.writeValueAsString(betRequest);
        RequestBuilder request = MockMvcRequestBuilders
                .post("/markets/addBet")
                .header("Authorization",jwt)
                .contentType(APPLICATION_JSON_UTF8)
                .content(requestJson);

        MvcResult result = mvc.perform(request).andReturn();
        assertEquals(200,result.getResponse().getStatus());
    }

    @Test
    void addBet_expected400() throws Exception {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken("moderator", "moderator123"));
        String jwt = jwtUtils.generateJwtToken(authentication);
        SecurityContextHolder.getContext().setAuthentication(authentication);
//

        Counter counter = counterRepository.findByName("markets");

        BetRequest betRequest = BetRequest.builder().marketId(counter.getValue()).chosenOption("test1").noPrice(0.5).yesPrice(0.5).shares(10000).build();

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        String requestJson=ow.writeValueAsString(betRequest);
        RequestBuilder request = MockMvcRequestBuilders
                .post("/markets/addBet")
                .header("Authorization",jwt)
                .contentType(APPLICATION_JSON_UTF8)
                .content(requestJson);

        MvcResult result = mvc.perform(request).andReturn();
        assertEquals(400,result.getResponse().getStatus());
    }

    @Test
    void removeBet_expected200() throws Exception {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken("moderator", "moderator123"));
        String jwt = jwtUtils.generateJwtToken(authentication);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        Counter counter = counterRepository.findByName("bets");

        RequestBuilder request = MockMvcRequestBuilders
                .post("/markets/deleteBet?betId="+counter.getValue())
                .header("Authorization",jwt)
                .contentType(APPLICATION_JSON_UTF8);

        MvcResult result = mvc.perform(request).andReturn();
        assertEquals(200,result.getResponse().getStatus());
    }

    @Test
    void createMarket_expected401() throws Exception {

        CreateMarketRequest createMarketRequest = CreateMarketRequest
                .builder().description("opis").predictedEndDate("2021-01-01").topic("topic3")
                .category("SPORT").build();

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        String requestJson=ow.writeValueAsString(createMarketRequest);
        RequestBuilder request = MockMvcRequestBuilders
                .post("/markets/new")
                .contentType(APPLICATION_JSON_UTF8)
                .content(requestJson);

        MvcResult result = mvc.perform(request).andReturn();
        System.out.println(result.getResponse().getStatus());
        assertEquals(401,result.getResponse().getStatus());
    }

}
