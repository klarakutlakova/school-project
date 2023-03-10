package com.gfa.users.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtConfiguration {

  @Value("${security.jwt.uri}")
  private String Uri;

  @Value("${security.jwt.header}")
  private String header;

  @Value("${security.jwt.prefix}")
  private String prefix;

  @Value("${security.jwt.expiration}")
  private String expiration;

  @Value("${security.jwt.secret}")
  private String secret;

  public JwtConfiguration() {
  }

  public String getUri() {
    return Uri;
  }

  public String getHeader() {
    return header;
  }

  public String getPrefix() {
    return prefix;
  }

  public String getExpiration() {
    return expiration;
  }

  public String getSecret() {
    return secret;
  }
}
