package com.storix.api.global.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class RequiredIfValidator implements ConstraintValidator<RequiredIf, Object> {

    private String when;
    private String is;
    private String field;

    @Override
    public void initialize(RequiredIf constraintAnnotation) {
        this.when = constraintAnnotation.when();
        this.is = constraintAnnotation.is();
        this.field = constraintAnnotation.field();
    }

    @Override
    public boolean isValid(Object request, ConstraintValidatorContext context) {
        if (request == null) return true;
        if (!String.valueOf(FieldAccess.read(request, when)).equals(is)) return true;
        if (present(FieldAccess.read(request, field))) return true;

        // 필드를 지정해야 응답의 fieldErrors 에 그 이름으로 잡힌다
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                .addPropertyNode(field)
                .addConstraintViolation();
        return false;
    }

    private boolean present(Object value) {
        if (value == null) return false;
        return !(value instanceof CharSequence text) || !text.toString().isBlank();
    }

}
