package com.surveyor.requests;

import com.surveyor.requests.data.dto.AnswerDTO;
import com.surveyor.requests.data.dto.QuestionDTO;
import com.surveyor.requests.data.dto.ResponseDTO;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.listener.config.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.rule.KafkaEmbedded;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@DirtiesContext
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RequestsApplicationTests {
    @ClassRule
    public static KafkaEmbedded embeddedKafka =
            new KafkaEmbedded(1, true, "answers"); //TODO ${spring.kafka.topic}
    @LocalServerPort
    private int port;
    @Autowired
    private TestRestTemplate restTemplate;
    private CountDownLatch latch;
    private Map<String, String> answersGot = new HashMap<>();

    @Autowired
    private KafkaConfiguration configuration;

    private KafkaMessageListenerContainer<String, QuestionDTO> container;

    @Before
    public void setUp() throws Exception {
        DefaultKafkaConsumerFactory<String, QuestionDTO> consumerFactory =
                configuration.consumerFactory(embeddedKafka);
        ContainerProperties containerProperties = new ContainerProperties("answers"); //TODO ${spring.kafka.topic}
        container = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);
        container.setupMessageListener((MessageListener<String, QuestionDTO>) record -> {
            QuestionDTO answered = record.value();
            latch.countDown();
            answersGot.put(answered.getQuestionId(), answered.getAnswerId());
        });
        container.start();
        ContainerTestUtils.waitForAssignment(container, embeddedKafka.getPartitionsPerTopic());
    }

    @After
    public void tearDown() {
        container.stop();
    }

    /**
     * Preload survey, call controller with answer ids, check answers in kafka.
     *
     * @throws InterruptedException
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:survey.sql")
    public void testAnswer() throws InterruptedException {
        List<AnswerDTO> answers = new ArrayList<>();
        answers.add(new AnswerDTO("answer_id1"));
        answers.add(new AnswerDTO("answer_id4"));
        answers.add(new AnswerDTO("answer_id7"));
        latch = new CountDownLatch(answers.size());
        ResponseDTO responseDTO =
                this.restTemplate.postForObject(
                        "http://localhost:" + port + "/respond/survey_id1",
                        answers,
                        ResponseDTO.class);
        Assert.assertTrue(responseDTO.isResult());
        Assert.assertNotNull(responseDTO.getResponse());
        latch.await(1000, TimeUnit.MILLISECONDS);
        Assert.assertEquals(0, latch.getCount());
        Assert.assertEquals(3, answers.size());
        Assert.assertEquals("answer_id1", answersGot.get("question_id1"));
        Assert.assertEquals("answer_id4", answersGot.get("question_id2"));
        Assert.assertEquals("answer_id7", answersGot.get("question_id3"));
    }
}
