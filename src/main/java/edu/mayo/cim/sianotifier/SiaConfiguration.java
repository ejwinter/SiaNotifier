package edu.mayo.cim.sianotifier;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RootUriTemplateHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * configuration for the connection to SIA
 */
@Configuration
public class SiaConfiguration {

    /**
     * Get a RESTTEmplate that can connect to SIA.  Currently SIA has not
     * security setup in front of its REST endpoints because they are only
     * GET requests and not considered confidential.
     *
     * @return the rest template that can be used to connect to SIA
     */
    @Bean(name = "SiaRestTemplate")
    public RestTemplate createSiaRestTempate(){
        return new RestTemplate();
    }


}
