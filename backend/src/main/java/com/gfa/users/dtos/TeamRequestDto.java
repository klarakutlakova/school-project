package com.gfa.users.dtos;

public class TeamRequestDto {
  String team;

  public TeamRequestDto(String team) {
    this.team = team;
  }

  public String getTeam() {
    return team;
  }
}