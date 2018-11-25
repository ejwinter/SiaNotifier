package edu.mayo.cim.sianotifier.sia;

import java.util.List;

/**
 * An assay as defined by the Sample Intake App
 *
 * There is other information available, we only mapped the fields we want from SIA.
 */
public class AssayDefinition {
    /** the identifier for this assay */
    private String projectNumber;
    /** all of the panels within this assay assays are divided into at least one panel */
    private List<PanelDefinition> panelDefinitions;
    /** people within the testing lab who should be contacted about events in this assay */
    private List<Contact> hostLabContacts;
    /** deve techs within core lab who should received notifications of events in the assay */
    private List<Contact> cgslDevTechs;

    public String getProjectNumber() {
        return projectNumber;
    }

    public AssayDefinition setProjectNumber(String projectNumber) {
        this.projectNumber = projectNumber;
        return this;
    }

    public List<PanelDefinition> getPanelDefinitions() {
        return panelDefinitions;
    }

    public AssayDefinition setPanelDefinitions(List<PanelDefinition> panelDefinitions) {
        this.panelDefinitions = panelDefinitions;
        return this;
    }

    public List<Contact> getHostLabContacts() {
        return hostLabContacts;
    }

    public AssayDefinition setHostLabContacts(List<Contact> hostLabContacts) {
        this.hostLabContacts = hostLabContacts;
        return this;
    }

    public List<Contact> getCgslDevTechs() {
        return cgslDevTechs;
    }

    public AssayDefinition setCgslDevTechs(List<Contact> cgslDevTechs) {
        this.cgslDevTechs = cgslDevTechs;
        return this;
    }
}
