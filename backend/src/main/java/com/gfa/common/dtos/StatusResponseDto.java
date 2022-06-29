package com.gfa.common.dtos;

import com.fasterxml.jackson.annotation.JsonCreator;

public class StatusResponseDto extends ResponseDto{
    String status;
    @JsonCreator
    public StatusResponseDto(String status) {
        this.status = status;
    }
}
