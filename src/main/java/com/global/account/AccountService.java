package com.global.account;

import com.global.domain.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.el.ELContext;
import javax.validation.Valid;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {
  private final AccountRepository accountRepository;
  private final JavaMailSender javaMailSender;
  private final PasswordEncoder passwordEncoder;
  // private final AuthenticationManager authenticationManager;

  @Transactional
  public Account processNewAccount(SignUpForm signUpForm) {
    Account newAccount = saveNewAccount(signUpForm);
    // 이메일 보내기 전에 토큰값 생성하기
    newAccount.generateEmailCheckToken();
    sendSignUpConfirmEmail(newAccount);
    return newAccount;
  }

  private Account saveNewAccount(@Valid SignUpForm signUpForm) {
    Account account = Account.builder()
      .email(signUpForm.getEmail())
      .nickName(signUpForm.getNickName())
      .password(passwordEncoder.encode(signUpForm.getPassword()))
      .studyCreateByWeb(true)
      .studyEnrollmentResultByWeb(true)
      .studyUpdateByWeb(true)
      .build();

    Account newAccount = accountRepository.save(account);
    return newAccount;
  }

  private void sendSignUpConfirmEmail(Account newAccount) {
    SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
    // 토큰값에 해당하는 이메일 주소 받기
    simpleMailMessage.setTo(newAccount.getEmail());
    // 이메일 제목
    simpleMailMessage.setSubject("회원 가입 인증");
    // 이메일 본문
    // simpleMailMessage.setText("/check-email-token?token=이메일보내기전에생성한토큰값&email=토큰값에해당하는이메일주소");
    simpleMailMessage.setText("/check-email-token?token=" + newAccount.getEmailCheckToken()
      + "&email=" + newAccount.getEmail());
    javaMailSender.send(simpleMailMessage);
  }

  // password 를 encoding 하기 때문에 아래의 방법으로 로그인함
  public void login(Account account) {
    UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                                                        account.getNickName(),
                                                        account.getPassword(),
                                                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                                                      );
    // Spring 에서 제공하는 SecurityContextHolder 에서
    // context 를 받아와서 인증하기
    SecurityContext context = SecurityContextHolder.getContext();
    context.setAuthentication(token);
  }
}
