package com.gfa.common.dtos;

import org.junit.jupiter.api.Test;


class IdRequestDtoTest {

  @Test
  void can_create_dto() {
    IdRequestDto idRequestDto = new IdRequestDto(55L);
    // assertEquals(55L, idRequestDto.getId());
  }
}
