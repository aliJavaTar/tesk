package com.ali.reservation.usecase;

import com.ali.reservation.infrastructure.persistence.entity.AvailableSlotEntity;
import com.ali.reservation.presentation.dto.reqeust.ReserveSlotRequest;
import com.ali.reservation.presentation.exption.ApplicationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class ReservationConcurrencyTest {

    @Container
    static final PostgreSQLContainer<?> postgreSQLContainer =
            new PostgreSQLContainer<>("postgres:17")
                    .withDatabaseName("test_db")
                    .withUsername("test_user")
                    .withPassword("test_password");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    }


    @Autowired
    private ConcurrencyTestHelper concurrencyHelper;

    @Test
    void reserveSlot_sameSlotTwoUsers_onlyOneSucceeds() {

        var slotTime = LocalDateTime.now().plusHours(1);
        var slot = concurrencyHelper.createSlotForConcurrencyTest(slotTime);
        var user1 = concurrencyHelper.createUserForConcurrencyTest("user1");
        var user2 = concurrencyHelper.createUserForConcurrencyTest("user2");

        ConcurrencyTestResult result = concurrencyHelper.executeSimultaneousReservations(slot, user1, user2);

        assertThat(result.successCount()).isEqualTo(1);
        assertThat(result.failureCount()).isEqualTo(1);
        assertSlotFinalStateIsConsistent(slot.getId());
    }
    @Test
    void reserveSlot_sameSlotTenUsers_onlyOneSucceeds() {
        var slotTime = LocalDateTime.now().plusHours(3);
        var slot = concurrencyHelper.createSlotForConcurrencyTest(slotTime);

        List<TestUser> users = IntStream.range(0, 10)
                .mapToObj(index -> concurrencyHelper.createUserForConcurrencyTest("user" + index)).toList();

        ConcurrencyTestResult result = concurrencyHelper.executeConcurrentReservations(slot, users);

        assertThat(result.successCount()).isEqualTo(1);
        assertThat(result.failureCount()).isEqualTo(9);
        assertSlotFinalStateIsConsistent(slot.getId());
    }

    @Test
    void reserveSlot_differentSlotsTwoUsers_bothSucceed() {

        var baseTime = LocalDateTime.now().plusHours(2);
        var slot1 = concurrencyHelper.createSlotForConcurrencyTest(baseTime);
        var slot2 = concurrencyHelper.createSlotForConcurrencyTest(baseTime.plusHours(1));
        var user1 = concurrencyHelper.createUserForConcurrencyTest("user1");
        var user2 = concurrencyHelper.createUserForConcurrencyTest("user2");

        ConcurrencyTestResult result = concurrencyHelper.executeParallelReservations(
            user1, slot1, user2, slot2);

        assertThat(result.successCount()).isEqualTo(2);
        assertThat(result.failureCount()).isEqualTo(0);
    }

    private void assertSlotFinalStateIsConsistent(Long slotId) {
        AvailableSlotEntity finalSlot = concurrencyHelper.findSlotById(slotId);
        long reservationCount = concurrencyHelper.countReservationsForSlot(slotId);

        assertThat(finalSlot.isReserved()).isTrue();
        assertThat(reservationCount).isEqualTo(1);
    }
}

