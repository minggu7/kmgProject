package com.kmgServer.project.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class RefreshDTO {
    private Long id;
    private String username;
    private String refresh;
    private String expiration;
}
