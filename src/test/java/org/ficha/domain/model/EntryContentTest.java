package org.ficha.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import java.util.List;

public class EntryContentTest {
    @Test
    @DisplayName("Should create content with observations")
    void shouldCreateContentWithObservations() {
        // Arrange
        String summary = "CBC";
        String note = "Within expected range";
        Observation observation = Observation.of("hemoglobin", "14.2", "ml/h");

        // Act (entry content creation)
        EntryContent content = EntryContent.of("CBC", "Within expected range", List.of(observation));

        // Assert
        assertThat(content.summary()).isEqualTo("CBC");
        assertThat(content.notes()).isEqualTo("Within expected range");
        assertThat(content.observations()).containsExactly(observation);
    }

    @Test
    @DisplayName("Should reject a blank summary")
    void shouldRejectBlankSummary() {
        // Act & Assert
        assertThatThrownBy(() -> EntryContent.of(" ", "notes", List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should reject blank observation fields")
    void shouldRejectBlankObservationFields() {
        // AAA made in a one-liner lambda
        assertThatThrownBy(() -> Observation.of("", "1", "u"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Observation.of("name", " ", "u"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Observation.of("name", "1", null))
                .isInstanceOf(NullPointerException.class);
    }
}
