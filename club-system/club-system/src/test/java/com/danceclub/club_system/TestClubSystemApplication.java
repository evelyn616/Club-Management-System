package com.danceclub.club_system;

import org.springframework.boot.SpringApplication;

public class TestClubSystemApplication {

	public static void main(String[] args) {
		SpringApplication.from(ClubSystemApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
