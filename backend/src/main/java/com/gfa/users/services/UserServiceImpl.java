package com.gfa.users.services;

import com.gfa.common.dtos.*;
import com.gfa.users.models.User;
import com.gfa.users.repositories.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class UserServiceImpl implements UserService {

  // private final BCryptPasswordEncoder bCryptPasswordEncoder;
  private final EmailValidator emailValidator;
  private final UserRepository userRepository;

  private final EmailServiceImpl emailServiceImpl;

  public UserServiceImpl(
      EmailValidator emailValidator,
      UserRepository userRepository,
      EmailServiceImpl emailServiceImpl) {
    this.emailValidator = emailValidator;
    this.userRepository = userRepository;
    this.emailServiceImpl = emailServiceImpl;
  }

  public ResponseEntity<? extends ResponseDto> store(CreateUserRequestDto dto) {

    if (dto.username.isEmpty()) {
      return new ResponseEntity<>(
          new ErrorResponseDto("Username is required"), HttpStatus.BAD_REQUEST);
    }
    if (dto.password.isEmpty()) {
      return new ResponseEntity<>(
          new ErrorResponseDto("Password is required"), HttpStatus.BAD_REQUEST);
    }
    if (dto.email.isEmpty()) {
      return new ResponseEntity<>(
          new ErrorResponseDto("Email is required"), HttpStatus.BAD_REQUEST);
    }

    boolean usernameExist = userRepository.findByUsername(dto.username).isPresent();
    if (usernameExist) {
      ErrorResponseDto error = new ErrorResponseDto("Username is already taken");
      return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    /*boolean userExist = userRepository.findByEmail(dto.email).isPresent();
    if(userExist){
        // throw new IllegalStateException("email already taken ");
        ErrorResponseDto error = new ErrorResponseDto("Email is already taken");
        return new ResponseEntity<>(error,HttpStatus.CONFLICT);
    }*/

    if (dto.username.length() < 4) {
      ErrorResponseDto error = new ErrorResponseDto("Username must be at least 4 characters long");
      return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    if (dto.password.length() < 8) {
      ErrorResponseDto error = new ErrorResponseDto("Password must be at least 8 characters long");
      return new ResponseEntity(error, HttpStatus.BAD_REQUEST);
    }

    boolean isValidEmail = emailValidator.isValid(dto.email);
    if (!isValidEmail) {
      // throw new IllegalStateException("email not valid");
      ErrorResponseDto error = new ErrorResponseDto("Invalid email");
      return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // String encodedPassword = bCryptPasswordEncoder.encode(user.getPassword());
    // user.setPassword(encodedPassword);

    User user = new User(dto);
    userRepository.save(user);
    UserResponseDto userResponseDto = new UserResponseDto(user);
    return new ResponseEntity<UserResponseDto>(userResponseDto, HttpStatus.CREATED);
  }

  @Override
  public ResponseEntity<? extends ResponseDto> resetPasswords(EmailRequestDto emailDto) {
    boolean isValidEmail = emailValidator.isValid(emailDto.email);
    User user = userRepository.findByEmail(emailDto.email);

    // 1. required, may not be empty, must be unique, must be a valid email address
    if (!isValidEmail || !emailDto.email.isEmpty() ){
      return new ResponseEntity<>(new ErrorResponseDto("Invalid email!"), HttpStatus.BAD_REQUEST);
    }

    // 2. Trying to reset a password for an unverfied email address results in “Unverified email!
    if (user.getVerifiedAt() == null){
      return new ResponseEntity<>(new ErrorResponseDto("Unverified email"), HttpStatus.BAD_REQUEST);
    }


    // 3. Status OK
    if (userRepository.existsByEmail(emailDto.email) && user.getVerifiedAt() != null) {
     // User user = userRepository.findByEmail(emailDto.email);                                       // find user by email
      user.setForgottenPasswordToken(UUID.randomUUID().toString());                                 // create a Token
      user.setForgottenPasswordTokenExpiresAt(
          new Date(System.currentTimeMillis() + expirationTime));                                  // set token with expiration --> expirationTime will be variable
      userRepository.save(user);


      // String appUrl = request.getScheme() + "://" + request.getServerName();                      // scheme of our URL ---> appUrl will be variable


      SimpleMailMessage passwordResetEmail = new SimpleMailMessage();                               // created mail
      passwordResetEmail.setFrom("support@demo");                                                // ?? set a emailFrom
      passwordResetEmail.setTo(user.getEmail());
      passwordResetEmail.setSubject("Password Reset Request");
      passwordResetEmail.setText(
          "To reset your password, click the link below:\n"
              + appUrl                                                                              // ---> appUrl will be variable
              + "/reset?token="
              + user.getForgottenPasswordToken());

      emailServiceImpl.sendEmail(passwordResetEmail);
      return new ResponseEntity<>(new StatusResponseDto("ok"), HttpStatus.OK);
    }

    // 4.The specified email address does not exist results in “ok”
    if (!userRepository.existsByEmail(emailDto.email)) {
      return new ResponseEntity<>(new StatusResponseDto("ok"), HttpStatus.OK);
    }
    return new ResponseEntity<>(new ErrorResponseDto("Something goes wrong"), HttpStatus.BAD_REQUEST);
  }

  @Override
  public ResponseEntity<? extends ResponseDto> resetPasswordViaToken(PasswordResetRequestDto resetPassword, String token) {
    User user = userRepository.findByForgottenPasswordToken(token);
    Date currentDate = new Date(System.currentTimeMillis());

    // 1.Request with an empty password results
    if (resetPassword.password.isEmpty()){
      return new ResponseEntity<>(new ErrorResponseDto("Password is required"), HttpStatus.BAD_REQUEST);
    }
    // 2.Request in an invalid token (token not found in the database) results in “Invalid token”
    if (!userRepository.existsByForgottenPasswordToken(token)){
      return new ResponseEntity<>(new ErrorResponseDto("Invalid token"), HttpStatus.BAD_REQUEST);
    }
    // 3. Request in an expired token (1h but configurable) results in “Expired token”
    if (currentDate.after(user.getVerificationTokenExpiresAt())){
      return new ResponseEntity<>(new ErrorResponseDto("Expired token"), HttpStatus.BAD_REQUEST);
    }
    // 4. Request with a password shorter than 8 characters results in “Password must be at least 8 characters long” error
    if (resetPassword.password.length() <= 8){
      return new ResponseEntity<>(new ErrorResponseDto("Password must be at least 8 characters long"), HttpStatus.BAD_REQUEST);

      /// send a new token to email (create the general method)
    }
    // 5. Request with a valid token and a valid password will result in the user’s password updated
    if (userRepository.existsByForgottenPasswordToken(token) && currentDate.before(user.getForgottenPasswordTokenExpiresAt())) {
      user.setPassword(resetPassword.password);
      user.setForgottenPasswordToken(null);                                                         // reset token value to null
      userRepository.save(user);
      return new ResponseEntity<>(new StatusResponseDto("ok"), HttpStatus.OK);
     }
    // 6. Request in the same password as currently set should just set the password and return the “ok” status message
    if (userRepository.existsByForgottenPasswordToken(token) && currentDate.before(user.getForgottenPasswordTokenExpiresAt()) && resetPassword.password.equals(user.getPassword())) {                                      //PROBLEM !!! will be same or not if there is a use hashing
      user.setPassword(resetPassword.password);
      user.setForgottenPasswordToken(null);
      userRepository.save(user);
      return new ResponseEntity<>(new StatusResponseDto("ok"), HttpStatus.OK);
    }
    return new ResponseEntity<>(new ErrorResponseDto("Something goes wrong"), HttpStatus.BAD_REQUEST);

  }


  @Override
  public ResponseEntity<ResponseDto> index() {
    return null;
  }

  @Override
  public ResponseEntity<ResponseDto> show(Long id) {
    return null;
  }

  /*@Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
      return userRepository.findByEmail(email)
              .orElseThrow(() -> new UsernameNotFoundException("User not found"));
  }*/
}
