package world.trecord.web.exception;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class ValidationErrorDTO {

    private List<FieldError> fieldErrors;

    @Builder
    private ValidationErrorDTO(List<FieldError> fieldErrors) {
        this.fieldErrors = fieldErrors;
    }

    @NoArgsConstructor
    @Getter
    @Setter
    public static class FieldError {
        private String field;
        private String message;

        @Builder
        private FieldError(String field, String message) {
            this.field = field;
            this.message = message;
        }
    }
}
