package edu.mayo.cim.sianotifier.sia;

import java.util.Objects;

/**
 * Someone who should get notifications of evens on assays or panels.
 *
 * Currently, we are only interested in email but other info is available
 */
public class Contact {
    private String email;

    public String getEmail() {
        return email;
    }

    public Contact setEmail(String email) {
        this.email = email;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Contact contact = (Contact) o;
        return Objects.equals(email, contact.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email);
    }
}
