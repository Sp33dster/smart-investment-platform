package com.speedster.investment.smart_investment_platform.user.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class UserTest {

    @Test
    @DisplayName("Should create user with USER role by default")
    void shouldCreateUserWithUserRole(){

        //given & when
        User user = User.builder()
                .email("jan@example.com")
                .password("hashedPassword")
                .firstName("Jan")
                .lastName("Kowalski")
                .role(Role.USER)
                .build();

        // then
        assertThat(user.getEmail()).isEqualTo("jan@example.com");
        assertThat(user.getRole()).isEqualTo(Role.USER);
        assertThat(user.getFirstName()).isEqualTo("Jan");
    }

    @Test
    @DisplayName("Should not expose password in toString")
    void shouldNotExposePasswordInToString() {
        // given
        User user = User.builder()
                .email("jan@example.com")
                .password("supersecretpassword")
                .firstName("Jan")
                .lastName("Kowalski")
                .role(Role.USER)
                .build();

        // when
        String userString = user.toString();

        // then
        assertThat(userString).doesNotContain("supersecretpassword");
    }
}
