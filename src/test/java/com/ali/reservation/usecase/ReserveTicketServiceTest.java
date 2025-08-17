package com.ali.reservation.usecase;

import com.ali.reservation.infrastructure.persistence.entity.AvailableSlotEntity;
import com.ali.reservation.infrastructure.persistence.entity.ReservationEntity;
import com.ali.reservation.infrastructure.persistence.entity.UserEntity;
import com.ali.reservation.infrastructure.persistence.repository.AvailableSlotRepository;
import com.ali.reservation.infrastructure.persistence.repository.ReservationRepository;
import com.ali.reservation.infrastructure.persistence.repository.UserRepository;
import com.ali.reservation.infrastructure.security.SecurityUtils;
import com.ali.reservation.presentation.dto.reqeust.ReserveSlotRequest;
import com.ali.reservation.presentation.dto.response.ReservationResponse;
import com.ali.reservation.presentation.exption.ApplicationException;
import com.ali.reservation.presentation.exption.EntityNotFountException;
import com.ali.reservation.presentation.exption.TimeNotValidException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mockStatic;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@Transactional
@ExtendWith(SpringExtension.class)
class ReserveTicketServiceTest {

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
    private ReserveTicketService reserveTicketService;

    @Autowired
    private TestDataFactory testDataFactory;

    private TestUser testUser;

    @BeforeEach
    void setUp() {
        testDataFactory.cleanDatabase();
        testUser = testDataFactory.createTestUser("test-user");
    }


    @Test
    void should_getAvailableSlots_withFutureTime_returnsAvailableSlots() {

        var futureTime = LocalDateTime.now().plusHours(1);
        testDataFactory.createAvailableSlot(futureTime);
        Pageable pageable = PageRequest.of(0, 10);

        Page<ReservationResponse> result = reserveTicketService.getAvailableSlots(futureTime, pageable);

        assertThat(result).hasSize(1);
    }

    @Test
    void getAvailableSlots_withReservedSlot_excludesReservedSlots() {

        var futureTime = LocalDateTime.now().plusHours(1);
        testDataFactory.createReservedSlot(futureTime);
        var pageable = PageRequest.of(0, 10);

        Page<ReservationResponse> result = reserveTicketService.getAvailableSlots(futureTime, pageable);

        assertThat(result).isEmpty();
    }

    @Test
    void do_not_allow_getAvailableSlots_withPastTime() {

        var pastTime = LocalDateTime.now().minusHours(1);
        var pageable = PageRequest.of(0, 10);

        assertThatThrownBy(() -> reserveTicketService.getAvailableSlots(pastTime, pageable))
                .isInstanceOf(TimeNotValidException.class);
    }


    @Test
    void reserveSlot_withAvailableSlot_marksSlotAsReserved() {
        var slotTime = LocalDateTime.now().plusHours(2);
        var slot = testDataFactory.createAvailableSlot(slotTime);
        var request = createReserveSlotRequest(slotTime);

        executeWithMockedUser(() -> reserveTicketService.reserveSlot(request));

        assertSlotIsReserved(slot.getId());
        assertReservationExists(testUser.id(), slot.getId());
    }

    @Test
    void do_not_allow_reserveSlot_withNonExistentSlot() {

        var nonExistentTime = LocalDateTime.now().plusHours(5);
        var request = createReserveSlotRequest(nonExistentTime);

        assertThatThrownBy(() -> executeWithMockedUser(() -> reserveTicketService.reserveSlot(request)))
                .isInstanceOf(ApplicationException.class);
    }

    @Test
    void do_not_allow_reserveSlot_withAlreadyReservedSlot() {

        var slotTime = LocalDateTime.now().plusHours(3);
        testDataFactory.createReservedSlot(slotTime);
        var request = createReserveSlotRequest(slotTime);

        assertThatThrownBy(() -> executeWithMockedUser(() -> reserveTicketService.reserveSlot(request)))
                .isInstanceOf(ApplicationException.class);
    }


    @Test
    void cancelReservation_withValidReservation_releasesSlot() {

        var slotTime = LocalDateTime.now().plusHours(3);
        var slot = testDataFactory.createReservedSlot(slotTime);
        testDataFactory.createReservation(testUser.id(), slot);

        executeWithMockedUser(() -> reserveTicketService.cancelReservation(slot.getId()));

        assertSlotIsNotReserved(slot.getId());
        assertReservationDoesNotExist(testUser.id(), slot.getId());
    }

    @Test
    void dont_allow_cancelReservation_withNonExistentReservation() {
        Long nonExistentSlotId = 999L;

        assertThatThrownBy(() -> executeWithMockedUser(() ->
                reserveTicketService.cancelReservation(nonExistentSlotId)))
                .isInstanceOf(EntityNotFountException.class);
    }

    @Test
    void dont_allow_cancelReservation_withOtherUserReservation() {
        var otherUser = testDataFactory.createTestUser("other-user");
        var slotTime = LocalDateTime.now().plusHours(4);
        var slot = testDataFactory.createReservedSlot(slotTime);
        testDataFactory.createReservation(otherUser.id(), slot);

        assertThatThrownBy(() -> executeWithMockedUser(() ->
                reserveTicketService.cancelReservation(slot.getId())))
                .isInstanceOf(EntityNotFountException.class);
    }


    private ReserveSlotRequest createReserveSlotRequest(LocalDateTime slotTime) {
        var request = new ReserveSlotRequest();
        request.setSlotStartTime(slotTime);
        return request;
    }

    private void executeWithMockedUser(Runnable action) {
        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(testUser.id());
            action.run();
        }
    }

    private void assertSlotIsReserved(Long slotId) {
        var slot = testDataFactory.findSlotById(slotId);
        assertThat(slot.isReserved()).isTrue();
    }

    private void assertSlotIsNotReserved(Long slotId) {
        var slot = testDataFactory.findSlotById(slotId);
        assertThat(slot.isReserved()).isFalse();
    }

    private void assertReservationExists(Long userId, Long slotId) {
        assertThat(testDataFactory.reservationExists(userId, slotId)).isTrue();
    }

    private void assertReservationDoesNotExist(Long userId, Long slotId) {
        assertThat(testDataFactory.reservationExists(userId, slotId)).isFalse();
    }
}
