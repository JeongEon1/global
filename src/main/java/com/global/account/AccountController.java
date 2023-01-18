package com.global.account;

import com.global.domain.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
public class AccountController {

  private final SignUpFormValidator signUpFormValidator;
  private final AccountService accountService;
  private final AccountRepository accountRepository;


  @InitBinder("signUpForm")
  public void initBinder(WebDataBinder webDataBinder){
    webDataBinder.addValidators(signUpFormValidator);

  }

  // localhost:8080/sign-up 을 주소표시줄에 입력했을 때
  // 자동으로 호출되는 메소드
  @GetMapping("/sign-up")
  public String signUpForm(Model model){
    model.addAttribute("signUpForm", new SignUpForm());
    return "account/sign-up";
  }

  // 회원가입 페이지에서 submit 버튼 눌렀을 때
  // 자동으로 호출되는 메소드
  @PostMapping("/sign-up")
  public String signUpSubmit(@Valid SignUpForm signUpForm, Errors errors){
    if(errors.hasErrors()){
      // 에러가 발생하면 다음 페이지로 넘어가지 않고
      // 다시 sign-up 페이지를 보여줌
      return "account/sign-up";
    }

    /*
    위에 작성한
    public void initBinder(WebDataBinder webDataBinder) 메소드에서
    검증 작업을 진행함

   ormValidator.validate(signUpForm, errors);
    if(errors.hasErrors()){
      // 에러가 발생하면 다음 페이지로 넘어가지 않고
      // 다시 sign-up 페이지를 보여줌
      return "account/sign-up";
    }
    */

    Account account = accountService.processNewAccount(signUpForm);
    accountService.login(account);
    /*
     이 부분은 Service 에서 실행하는 부분이라서
     Controller 에서 작성하지 않는 것이 좋음

    Account newAccount = accountService.saveNewAccount(signUpForm);
    // 이메일 보내기 전에 토큰값 생성하기
    newAccount.generateEmailCheckToken();
    accountService.sendSignUpConfirmEmail(newAccount);
   */
    // 회원가입 폼이 제대로 입력된 경우,
    // Home(/) 으로 이동함

    return "redirect:/";
  }

  // String token  <-- 가입하면서 받아온 token
  @GetMapping("/check-email-token")
  public String checkEmailToken(String token, String email, Model model){
    // 가입자가 입력한 이메일이 정상적으로 등록되었는지 확인하기
    Account account = accountRepository.findByEmail(email);

    // 이동할 page
    String view = "account/check-email";

    // 가입자가 입력한 이메일이 정상적으로 등록 안 된 경우
    if (account == null){
      model.addAttribute("error", "wrong email");
      return view;
    }

    if (!account.isValidToken(token)){
      model.addAttribute("error", "wrong email");
      return view;
    }

    // 가입자가 입력한 이메일이 정상적으로 등록된 경우
    // token 확인하기
    if (!account.getEmailCheckToken().equals(token)){
      model.addAttribute("error", "wrong email");
      return view;
    }

    account.completeSignUp();
    accountService.login(account);
    /*
    아래 code 를 Account 클래스의 completeSignUp() 메소드로 옮기기

    // 가입자가 입력한 이메일이 정상적으로 등록된 경우 + token 까지 확인된 경우
    account.setEmailVerified(true);
    // 가입한 시간 등록
    account.setJoinedAt(LocalDateTime.now());
    */
    // 몇 번째(accountRepository.count()) 가입자인지... 처리하기
    model.addAttribute("numberOfUser", accountRepository.count());
    // nickname
    model.addAttribute("nickName", account.getNickName());

    return view;
  }

}
