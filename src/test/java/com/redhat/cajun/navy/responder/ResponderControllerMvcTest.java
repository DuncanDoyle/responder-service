package com.redhat.cajun.navy.responder;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.redhat.cajun.navy.responder.listener.ResponderCommandMessageListener;
import com.redhat.cajun.navy.responder.model.Responder;
import com.redhat.cajun.navy.responder.service.ResponderService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.MimeTypeUtils;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {
       RespondersController.class, ResponderService.class
})
public class ResponderControllerMvcTest {

    @MockBean
    private ResponderService responderService;

    @Autowired
    private RespondersController controller;

    private MockMvc mockMvc;

    @MockBean
    private ResponderCommandMessageListener responderCommandMessageListener;

    @Captor
    private ArgumentCaptor<Responder> responderCaptor;

    @Before
    public void initTest() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();
    }

    @Test
    public void testAvailableResponders() throws Exception {

        initService();

        final ResultActions result = mockMvc.perform(
                get("/responders/available").accept(MimeTypeUtils.APPLICATION_JSON_VALUE));

        result.andExpect(status().isOk());
        result.andExpect(jsonPath("$.length()").value(2));

    }

    @Test
    public void testCreateResponder() throws Exception {

        String json = "{" +
                "\"name\" : \"John Doe\"," +
                "\"phoneNumber\" : \"111-222-333\"," +
                "\"latitude\" : 30.12345," +
                "\"longitude\" : -70.98765," +
                "\"boatCapacity\" : 3," +
                "\"medicalKit\" : true," +
                "\"available\": true" +
                "}";

        final ResultActions result = mockMvc.perform(post("/responders/responder")
                .contentType(MimeTypeUtils.APPLICATION_JSON_VALUE).content(json));

        result.andExpect(status().isCreated());
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
