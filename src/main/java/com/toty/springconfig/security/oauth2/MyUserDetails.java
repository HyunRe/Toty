package com.toty.springconfig.security.oauth2;

import com.toty.user.domain.model.User;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

public class MyUserDetails implements UserDetails, OAuth2User {

    private User user;

    private Map<String, Object> attributes;

    public MyUserDetails() { }
    public MyUserDetails(User user) {
        this.user = user;
    }
    public MyUserDetails(User user, Map<String, Object> attributes) {
        this.user = user;
        this.attributes = attributes;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> collection = new ArrayList<>();
        collection.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
//                System.out.println("getAuthority(): " + user.getRole());
                return String.valueOf(user.getRole());
            }
        });
        return collection;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() { return user.getEmail(); }

    @Override
    public String getName() { return String.valueOf(user.getId()); }
}
