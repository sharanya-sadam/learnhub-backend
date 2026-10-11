package com.learnhub.dto.user;

import com.learnhub.entity.Role;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateUserRequest {
    private String name;
    private String bio;
    private String avatarUrl;
    private Role role; // Admin only
    private Boolean active; // Admin only
}
