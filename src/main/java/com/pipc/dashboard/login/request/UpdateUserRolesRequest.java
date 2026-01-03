package com.pipc.dashboard.login.request;

import java.util.List;

import lombok.Data;

@Data
public class UpdateUserRolesRequest {
    private String username;
    private List<String> roles;
    
}