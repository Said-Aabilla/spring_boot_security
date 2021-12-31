package com.example.springsecurity.enumeration;

import static com.example.springsecurity.constants.Authority.*;

public enum Role {
    ROLE_USER(USER_AUTHORITIES),
    ROLE_HR(HR_AUTHORITIES),
    ROLE_MANAGER(MANAGER_AUTHORITIES),
    ROLE_ADMIN(ADMIN_AUTHORITIES),
    ROLE_SUPER_USER(SUPER_ADMIN_AUTHORITIES);

    private String[] userAuthorities;

    Role(String... userAuthorities) {
        this.userAuthorities = userAuthorities;
    }

    public String[] getUserAuthorities() {
        return userAuthorities;
    }
}
