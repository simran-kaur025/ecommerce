package com.bootcamp.ecommerce;

import com.bootcamp.ecommerce.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    public static User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        CustomUserDetails userDetails =
                (CustomUserDetails) authentication.getPrincipal();

        return userDetails.getUser();
    }
}
