package com.redhat.cajun.navy.responder;

import java.util.List;

import com.redhat.cajun.navy.responder.model.Responder;
import com.redhat.cajun.navy.responder.model.ResponderStats;
import com.redhat.cajun.navy.responder.service.ResponderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/responders")
public class RespondersController {

    @Autowired
    private ResponderService responderService;

    @RequestMapping(value = "/stats", method = RequestMethod.GET, produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public ResponderStats stats() {
        return responderService.getResponderStats();
    }

    @RequestMapping(value = "/responder/{id}", method = RequestMethod.GET, produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public ResponseEntity<Responder> responder(@PathVariable long id) {
        Responder responder = responderService.getResponder(id);
        if (responder == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(responder, HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/available", method = RequestMethod.GET, produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Responder>> activeResponders() {
        return new ResponseEntity<>(responderService.availableResponders(), HttpStatus.OK);
    }

    @RequestMapping(value = "/responder", method = RequestMethod.POST, consumes = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public ResponseEntity createResponder(@RequestBody Responder responder) {

        responderService.createResponder(responder);
        return new ResponseEntity(HttpStatus.CREATED);
    }

}
