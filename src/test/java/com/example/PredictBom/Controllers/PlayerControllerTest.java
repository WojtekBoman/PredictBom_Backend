package com.example.PredictBom.Controllers;

import com.example.PredictBom.Entities.ERole;
import com.example.PredictBom.Entities.Player;
import com.example.PredictBom.Entities.Role;
import com.example.PredictBom.Entities.User;
import com.example.PredictBom.Repositories.RoleRepository;
import com.example.PredictBom.Security.JWT.JwtUtils;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
class PlayerControllerTest {


    public static final MediaType APPLICATION_JSON_UTF8 = new MediaType(MediaType.APPLICATION_JSON.getType(), MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));

    @Autowired
    private MockMvc mvc;

    @Autowired
    private WebApplicationContext context;

    @MockBean
    AuthenticationManager authenticationManager;

    @MockBean
    JwtUtils jwtUtils;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    private static final String CONNECTION_STRING = "mongodb://%s:%d";

    private MongodExecutable mongodExecutable;
    private MongoTemplate mongoTemplate;


    @AfterEach
    void clean() {
        mongodExecutable.stop();
    }

    @BeforeEach
    void setup() throws Exception {
        String ip = "localhost";
        int port = 27017;

        IFeatureAwareVersion version = de.flapdoodle.embed.mongo.distribution.Versions.withFeatures(
                new GenericVersion("4.0.0"),
                Version.Main.PRODUCTION.getFeatures());

        IMongodConfig mongodConfig = new MongodConfigBuilder().version(version)
                .net(new Net(ip, port, Network.localhostIsIPv6()))
                .build();

        MongodStarter starter = MongodStarter.getDefaultInstance();
        mongodExecutable = starter.prepare(mongodConfig);
        mongodExecutable.start();
        mongoTemplate = new MongoTemplate(MongoClients.create(String.format(CONNECTION_STRING, ip, port)), "predict_bom");

        Role player = new Role(ERole.ROLE_PLAYER);
        Role mod = new Role(ERole.ROLE_MODERATOR);
//
        mongoTemplate.save(player,"roles");
        mongoTemplate.save(mod,"roles");

        Player playerUser = new Player("RynkoznawcaWojtek", "Wojtek", "Boman",
                "boman@gmail.com",
                encoder.encode("wojtek123"), 1000);

        Optional<Role> userRole = roleRepository.findByName(ERole.ROLE_MODERATOR);


        User moder = new User("moderator", "Wojtek", "Boman",
                "mod@gmail.com",
                encoder.encode("moderator123"));
        Set<Role> set = new HashSet<Role>();
        set.add(userRole.get());
        moder.setRoles(set);
        mongoTemplate.save(playerUser,"users");
        mongoTemplate.save(moder,"users");
    }


    @Test
    void getPlayerData() throws Exception {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken("RynkoznawcaWojtek", "wojtek123"));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        RequestBuilder request = MockMvcRequestBuilders
                .get("/player/RynkoznawcaWojtek")
//                .header("Authorization",jwt)
                .contentType("application/json");
        MvcResult result = mvc.perform(request).andReturn();
        assertEquals(200,result.getResponse().getStatus());
    }

    @Test
    void getRanking() throws Exception {
//        Authentication authentication = authenticationManager.authenticate(
//                new UsernamePasswordAuthenticationToken("RynkoznawcaWojtek", "wojtek123"));
//
//        SecurityContextHolder.getContext().setAuthentication(authentication);
//        String jwt = jwtUtils.generateJwtToken(authentication);

        RequestBuilder request = MockMvcRequestBuilders
                .get("/player/ranking")
                .contentType("application/json");
        MvcResult result = mvc.perform(request).andReturn();
        assertEquals(200,result.getResponse().getStatus());
    }
}
