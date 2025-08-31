package com.uber.payloads;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthResponseDto {

    private String token;
    private String tokenType = "Bearer";

}
