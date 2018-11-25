package edu.mayo.cim.sianotifier.sia;

import com.google.common.base.MoreObjects;

import java.time.ZonedDateTime;
import java.util.Set;

/**
 * A panel as defined within an assay.
 */
public class PanelDefinition {

    /**
     * the project number from the assay
     *
     * this is actually cloned from the assay and is not received from SIA
     */
    private String projectNumber;

    /**
     * everyone who should get notified of events on the panel
     *
     * this is actually cloned from the assay and is not received from SIA
     */
    private Set<Contact> notificationContacts;

    private String mnemonic;

    private ZonedDateTime goLiveDate;

    private String phase;

    public String getProjectNumber() {
        return projectNumber;
    }

    public PanelDefinition setProjectNumber(String projectNumber) {
        this.projectNumber = projectNumber;
        return this;
    }

    public String getMnemonic() {
        return mnemonic;
    }

    public PanelDefinition setMnemonic(String mnemonic) {
        this.mnemonic = mnemonic;
        return this;
    }

    public ZonedDateTime getGoLiveDate() {
        return goLiveDate;
    }

    public PanelDefinition setGoLiveDate(ZonedDateTime goLiveDate) {
        this.goLiveDate = goLiveDate;
        return this;
    }

    public String getPhase() {
        return phase;
    }

    public PanelDefinition setPhase(String phase) {
        this.phase = phase;
        return this;
    }

    public Set<Contact> getNotificationContacts() {
        return notificationContacts;
    }

    public PanelDefinition setNotificationContacts(Set<Contact> notificationContacts) {
        this.notificationContacts = notificationContacts;
        return this;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("projectNumber", projectNumber)
                .add("mnemonic", mnemonic)
                .add("goLiveDate", goLiveDate)
                .add("phase", phase)
                .toString();
    }
}
