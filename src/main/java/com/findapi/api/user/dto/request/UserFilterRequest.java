package com.findapi.api.user.dto.request;

import com.findapi.api.enums.UserRole;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserFilterRequest {
    private String name;
    private String email;
    private UserRole role;
}
