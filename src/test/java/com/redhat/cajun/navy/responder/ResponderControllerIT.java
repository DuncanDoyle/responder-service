package com.redhat.cajun.navy.responder;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import com.redhat.cajun.navy.responder.dao.ResponderDao;
import com.redhat.cajun.navy.responder.listener.ResponderCommandMessageListener;
import com.redhat.cajun.navy.responder.model.Responder;
import com.redhat.cajun.navy.responder.service.ResponderService;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.util.UriComponentsBuilder;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration(exclude= {KafkaAutoConfiguration.class, HibernateJpaAutoConfiguration.class})

public class ResponderControllerIT {

    @Value("${local.server.port}")
    private int port;

    @MockBean
    private ResponderService responderService;

    @MockBean
    private ResponderCommandMessageListener responderCommandMessageListener;

    @MockBean
    private ResponderDao rideDao;

    @Captor
    private ArgumentCaptor<Responder> responderCaptor;

    @Before
    public void initTest() {
        RestAssured.baseURI = String.format("http://localhost:%d", port);
    }

    @Test
    public void testAvailableResponders() {

        initService();

        given().request().get("/responders/available")
                .then()
                .assertThat()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", is(2));

    }

    @Test
    public void testCreateResponder() {

        String json = "{" +
                "\"name\" : \"John Doe\"," +
                "\"phoneNumber\" : \"111-222-333\"," +
                "\"latitude\" : 30.12345," +
                "\"longitude\" : -70.98765," +
                "\"boatCapacity\" : 3," +
                "\"medicalKit\" : true," +
                "\"available\": true" +
                "}";

        given().request().contentType(MimeTypeUtils.APPLICATION_JSON_VALUE).body(json).post("/responder")
                .then()
                .assertThat()
                .statusCode(201);

        verify(responderService).createResponder(responderCaptor.capture());
        Responder responder = responderCaptor.getValue();
        assertThat(responder, notNullValue());
        assertThat(responder.getName(), equalTo("John Doe"));
        assertThat(responder.getPhoneNumber(), equalTo("111-222-333"));
        assertThat(responder.getLatitude(), equalTo(new BigDecimal("30.12345")));
        assertThat(responder.getLongitude(), equalTo(new BigDecimal("-70.98765")));
        assertThat(responder.getBoatCapacity(), equalTo(3));
        assertThat(responder.isMedicalKit(), equalTo(true));
        assertThat(responder.isAvailable(), equalTo(true));
    }

    @Test
    public void testFindByName() {

        Responder responder = new Responder.Builder("1")
                .name("John Doe")
                .phoneNumber("111-222-333")
                .latitude(new BigDecimal("30.12345"))
                .longitude(new BigDecimal("-70.98765"))
                .boatCapacity(3)
                .medicalKit(true)
                .available(true)
                .build();

        when(responderService.getResponderByName(any(String.class))).thenReturn(responder);

        URI url = UriComponentsBuilder.fromUriString("/responder/byname").pathSegment("John Doe").build().encode().toUri();
        given().request().get(url.toASCIIString())
                .then()
                .assertThat()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("name", equalTo("John Doe"));
    }

    @Test
    public void testFindByNameWhenNotFound() {

        when(responderService.getResponderByName(any(String.class))).thenReturn(null);

        URI url = UriComponentsBuilder.fromUriString("/responder/byname").pathSegment("John Doe").build().encode().toUri();
        given().request().get(url.toASCIIString())
                .then()
                .assertThat()
                .statusCode(404);
    }

    private void initService() {
        Responder responder1 = new Responder.Builder("1")
                .name("John Doe")
                .phoneNumber("111-222-333")
                .latitude(new BigDecimal("30.12345"))
                .longitude(new BigDecimal("-70.98765"))
                .boatCapacity(3)
                .medicalKit(true)
                .available(true)
                .build();

        Responder responder2 = new Responder.Builder("2")
                .name("John Foo")
                .phoneNumber("999-888-777")
                .latitude(new BigDecimal("35.12345"))
                .longitude(new BigDecimal("-75.98765"))
                .boatCapacity(2)
                .medicalKit(true)
                .available(true)
                .build();

        List<Responder> responders = new ArrayList<>();
        responders.add(responder1);
        responders.add(responder2);

        when(responderService.availableResponders()).thenReturn(responders);
    }

}
