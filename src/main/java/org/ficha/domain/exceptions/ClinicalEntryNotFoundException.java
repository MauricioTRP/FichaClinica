package org.ficha.domain.exceptions;

public class ClinicalEntryNotFoundException extends RuntimeException {
    public ClinicalEntryNotFoundException(String message) {
        super(message);
    }
}
