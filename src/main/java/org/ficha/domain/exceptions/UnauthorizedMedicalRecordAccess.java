package org.ficha.domain.exceptions;

public class UnauthorizedMedicalRecordAccess extends RuntimeException {
    public UnauthorizedMedicalRecordAccess(String message) {
        super(message);
    }
}
