package org.ficha.application;

import org.ficha.domain.repository.MedicalRecordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class MedicalRecordApplicationServiceTest {

    @Mock
    private MedicalRecordRepository repository;

    @Test
    void rejectsNullRepository() {
        assertThatThrownBy(() -> new MedicalRecordApplicationService(null))
                .isInstanceOf(NullPointerException.class);
    }
}
