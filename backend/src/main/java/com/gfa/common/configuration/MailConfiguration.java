package com.gfa.common.configuration;

import java.util.Properties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
public class MailConfiguration {
  private final Environment environment;

  @Autowired
  public MailConfiguration(Environment environment) {
    this.environment = environment;
  }

  @Bean
  public JavaMailSender getJavaMailSender() {
    JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
    mailSender.setHost(environment.getProperty("spring.mail.host"));
    mailSender.setPort(Integer.parseInt(environment.getProperty("spring.mail.port")));
    mailSender.setUsername(environment.getProperty("spring.mail.username"));
    mailSender.setPassword(environment.getProperty("spring.mail.password"));

    mailSender.setJavaMailProperties(getProperties());
    return mailSender;
  }

  private Properties getProperties() {
    Properties properties = new Properties();
    properties.setProperty("mail.transport.protocol", "smtp");
    properties.setProperty("mail.smtp.auth", "true");
    properties.setProperty("mail.smtp.starttls.enable", "true");
    properties.setProperty("mail.debug", "true");
    return properties;
  }
}
