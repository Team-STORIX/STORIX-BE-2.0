package com.storix.api.global.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class FieldsCompareValidator implements ConstraintValidator<FieldsCompare, Object> {

    private String before;
    private String after;
    private String field;
    private boolean allowEqual;

    @Override
    public void initialize(FieldsCompare constraintAnnotation) {
        this.before = constraintAnnotation.before();
        this.after = constraintAnnotation.after();
        this.field = constraintAnnotation.field();
        this.allowEqual = constraintAnnotation.allowEqual();
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public boolean isValid(Object request, ConstraintValidatorContext context) {
        if (request == null) return true;

        Object left = FieldAccess.read(request, before);
        Object right = FieldAccess.read(request, after);
        if (left == null || right == null) return true;

        int compared = ((Comparable) left).compareTo(right);
        if (compared < 0 || (allowEqual && compared == 0)) return true;

        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                .addPropertyNode(field)
                .addConstraintViolation();
        return false;
    }
}
