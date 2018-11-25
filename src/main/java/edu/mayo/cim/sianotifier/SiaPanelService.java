package edu.mayo.cim.sianotifier;

import edu.mayo.cim.sianotifier.sia.AssayDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

/**
 * Used to access Sample Intake application (SIA).
 */
@Service
public class SiaPanelService {

    /** a rest template for connecting to SIA */
    private final RestTemplate siaRestTemplate;

    @Autowired
    public SiaPanelService(RestTemplate siaRestTemplate) {
        this.siaRestTemplate = siaRestTemplate;
    }

    public List<AssayDefinition> listAllAssays(){
        ResponseEntity<AssayDefinition[]> definitions = siaRestTemplate.getForEntity("/assay-definitions", AssayDefinition[].class);
        if(definitions.getStatusCode().is2xxSuccessful()) {
            return Arrays.asList(definitions.getBody());
        }else{
            throw new IllegalStateException("We did not get back an expected status code from SIA: " + definitions.getStatusCode());
        }
    }
}
