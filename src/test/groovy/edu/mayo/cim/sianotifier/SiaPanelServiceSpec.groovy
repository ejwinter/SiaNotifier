package edu.mayo.cim.sianotifier

import edu.mayo.cim.sianotifier.sia.AssayDefinition
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import spock.lang.Specification
import spock.lang.Subject

class SiaPanelServiceSpec extends Specification {

    @Subject
    SiaPanelService service

    RestTemplate restTemplate

    def setup(){
        restTemplate = Mock(RestTemplate)
        service = new SiaPanelService(restTemplate)
    }

    def "listAllAssays OK"(){

        given:
        AssayDefinition[] assays = [new AssayDefinition().setProjectNumber("NGS##")] as AssayDefinition[]
        and:
        restTemplate.getForEntity("/assay-definitions", AssayDefinition[].class) >> new ResponseEntity<AssayDefinition[]>(assays, HttpStatus.OK)

        when:
        List<AssayDefinition> list = service.listAllAssays()

        then:
        list.size() == 1
        list[0].getProjectNumber() == "NGS##"

    }

    def "listAllAssays NOT FOUND"(){

        given:
        restTemplate.getForEntity("/assay-definitions", AssayDefinition[].class) >> new ResponseEntity<AssayDefinition[]>(HttpStatus.NOT_FOUND)

        when:
        List<AssayDefinition> list = service.listAllAssays()


        then:
        IllegalStateException e = thrown()

    }
}
