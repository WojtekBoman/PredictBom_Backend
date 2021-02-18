package com.example.PredictBom.Services;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.jupiter.api.Assertions.*;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CounterServiceTest {

    @Autowired
    private CounterService counterService;

    @Test
    public void getNextId() {
        int currentValue = counterService.getCurrentValue("bets");
        int nextId = counterService.getNextId("bets");
        assertEquals(currentValue,nextId-1);
    }

}
