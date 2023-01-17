package com.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AppConfig {

  // PasswordEncoder 객체를 반환하는 메소드
  @Bean
  public PasswordEncoder passwordEncoder(){
    // bcrypt encoder 를 사용하게 됨
    return PasswordEncoderFactories.createDelegatingPasswordEncoder();
  }

}
