package com.global.account;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class SignUpFormValidator implements Validator {

  // DB 를 조회해서 email 이나 nickname 이 중복되는지 검사하려면
  // AccountRepository 가 있어야 함
  private final AccountRepository accountRepository;

  @Override
  public boolean supports(Class<?> clazz) {
    // SignUpForm 타입의 instance 를 조회함
    return clazz.isAssignableFrom((SignUpForm.class));
  }


  // Object target : form 에서 전달된 객체
  @Override
  public void validate(Object target, Errors errors) {
    // DB를 조회해서 email 이나 nickname 이 중복되는지 검사하기
    SignUpForm signUpForm = (SignUpForm)target;
    if(accountRepository.existsByEmail(signUpForm.getEmail())){
      errors.rejectValue("email", "invalid.email",
                          new Object[]{signUpForm.getEmail()},
                          "이미 사용 중인 이메일입니다");
    }
    if(accountRepository.existsByNickName(signUpForm.getNickName())){
      errors.rejectValue("nickName", "invalid.nickname",
                         new Object[]{signUpForm.getNickName()},
                        "이미 사용 중인 닉네임입니다");
    }

  }
}
