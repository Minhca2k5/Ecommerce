package com.minzetsu.ecommerce.common.validation;

import com.minzetsu.ecommerce.user.dto.request.PasswordRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, PasswordRequest> {
    @Override
    public boolean isValid(PasswordRequest passwordRequest, ConstraintValidatorContext constraintValidatorContext) {
        if(passwordRequest == null){
            return true;
        }
        String newPassword = passwordRequest.getNewPassword();
        String confirmPassword = passwordRequest.getConfirmPassword();
        if (newPassword == null || confirmPassword == null) {
            return true;
        }
        boolean matched = newPassword.equals(confirmPassword);
        if(!matched){
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext.buildConstraintViolationWithTemplate("New password and confirm password do not match")
                    .addPropertyNode("confirmPassword")
                    .addConstraintViolation();
        }
        return matched;
    }
}



