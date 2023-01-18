package com.global.account;

import com.global.domain.Account;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class AccountControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private AccountRepository accountRepository;

  @MockBean
  JavaMailSender javaMailSender;

  @DisplayName("인증 메일 확인 - 입력값이 잘못 된 경우")
  @Test
  void checkEmailToken_with_wrong_input() throws Exception{
    mockMvc.perform(get("/check-email-token")
      .param("token", "werwrefsfdsdfwef")
      .param("email", "testemail@google.com"))
      .andExpect(status().isOk())
      .andExpect(model().attributeExists("error"))
      .andExpect(view().name("account/check-email"))
      .andExpect(unauthenticated());
  }

  @DisplayName("인증 메일 확인 - 입력값이 맞는 경우")
  @Test
  void checkEmailToken_with_correct_input() throws Exception{
     Account account = Account.builder()
                             .email("test@gmail.com")
                             .password("12345678")
                             .nickName("global1000")
                             .build();
    //  위에서 생성한 account 객체로
    // accountRepository.save(account) 실행해서
    // 새로 생성된 newAccount 에서 generateEmailCheckToken() 실행하면
    // email token 이 생성되고
    // 이것을
    // .param("token", newAccount.getEmailCheckToken()) 에 넣어줌

    Account newAccount = accountRepository.save(account);
    newAccount.generateEmailCheckToken();

    mockMvc.perform(get("/check-email-token")
           .param("token", newAccount.getEmailCheckToken())
           .param("email", newAccount.getEmail()))
           .andExpect(status().isOk())
           .andExpect(model().attributeDoesNotExist("error"))
           .andExpect(model().attributeExists("nickName"))
           .andExpect(model().attributeExists("numberOfUser"))
           .andExpect(view().name("account/check-email"))
           .andExpect(authenticated().withUsername("global1000"));
  }

  @DisplayName("회원 가입 화면 테스트 - 화면이 보이는지 ...")
  @Test
  void signUpForm() throws Exception{
    mockMvc.perform(get("/sign-up"))
           .andDo(print())
           .andExpect(status().isOk())
           .andExpect(view().name("account/sign-up"))
           .andExpect(model().attributeExists("signUpForm"))
           .andExpect(unauthenticated());

  }

  // csrf (Cross Site Request Forgery) : Spring Security 에서 보안 수준을 향상시키는 기능
  @DisplayName("회원 가입 처리 확인하기 - 입력값 오류 테스트")
  @Test
  void signUpSubmit_with_wrong_input() throws Exception{
    mockMvc.perform(post("/sign-up")
           .param("nickName", "global")
           .param("email", "email......")
           .param("password", "1234")
           .with(csrf()))
           .andExpect(status().isOk())
           .andExpect(view().name("account/sign-up"))
           .andExpect(unauthenticated());
  }

  @DisplayName("회원 가입 처리 확인하기 - 입력값 정상인 경우")
  @Test
  void signUpSubmit_with_correct_input() throws Exception{
    mockMvc.perform(post("/sign-up")
           .param("nickName", "global1000")
           .param("email", "test@gmail.com")
           .param("password", "12345678")
           .with(csrf()))
           .andExpect(status().is3xxRedirection())
           .andExpect(view().name("redirect:/"))
           .andExpect(authenticated().withUsername("global1000"));


    Account account = accountRepository.findByEmail("test@gmail.com");
    // null 이 아닌지 확인하기
    assertNotNull(account);
    // 회원가입할 때 입력한 값과 encoding 된 값이 일치하지 않는지 확인하기
    assertNotEquals(account.getPassword(), "12345678");
    // token 값이 Null 이 아닌지(제대로 DB 에 저장되었는지) 확인하기
    assertNotNull(account.getEmailCheckToken());
    // 이메일 확인하기
    assertTrue(accountRepository.existsByEmail("test@gmail.com"));
    // mail 을 보내는지 test 하기
    then(javaMailSender).should().send(any(SimpleMailMessage.class));
  }


}