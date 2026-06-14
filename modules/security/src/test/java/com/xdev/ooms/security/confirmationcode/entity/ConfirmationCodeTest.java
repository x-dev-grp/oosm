package com.xdev.ooms.security.confirmationcode.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ConfirmationCodeTest {

    @Test
    void reportsExpiredAfterTenMinutes() {
        ConfirmationCode code = new ConfirmationCode();
        code.setLastModifiedDate(LocalDateTime.now().minusMinutes(11));

        assertThat(code.isExpired()).isTrue();
    }

    @Test
    void reportsConsumedOnlyAfterConsumptionTimestampIsSet() {
        ConfirmationCode code = new ConfirmationCode();

        assertThat(code.isConsumed()).isFalse();

        code.setConsumedAt(LocalDateTime.now());

        assertThat(code.isConsumed()).isTrue();
    }
}
