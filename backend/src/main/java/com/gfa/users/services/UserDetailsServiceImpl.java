package com.gfa.users.services;

import com.gfa.users.models.UserDetailsEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserService userService;
    @Autowired
    public UserDetailsServiceImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //User user = userRepository.findByUsername(username);
        //if(user == null){
                //throw new UsernameNotFoundException("Username not found"));
        // return new UserDetailsEntity(user);

        return userService
                .findByUsername(username)
                .map(UserDetailsEntity::new)
                .orElseThrow(() -> new UsernameNotFoundException("Username not found"));
    }
}
