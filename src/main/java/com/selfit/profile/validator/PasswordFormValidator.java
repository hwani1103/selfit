package com.selfit.profile.validator;

import com.selfit.profile.form.PasswordForm;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;



public class PasswordFormValidator implements Validator {
    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.isAssignableFrom(PasswordForm.class);
    }

    @Override
    public void validate(Object target, Errors errors) {
        PasswordForm passwordForm = (PasswordForm) target;

        if(!passwordForm.getNewPassword().equals(passwordForm.getNewPasswordConfirm())){
            errors.rejectValue("newPassword", "wrong.value", "입력하신 패스워드가 서로 일치하지 않습니다. 다시 시도해 주세요.");
        }

    }
}
