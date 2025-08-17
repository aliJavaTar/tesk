package com.ali.reservation.usecase;

import com.ali.reservation.infrastructure.persistence.entity.AvailableSlotEntity;
import com.ali.reservation.infrastructure.persistence.entity.ReservationEntity;
import com.ali.reservation.infrastructure.persistence.entity.UserEntity;
import com.ali.reservation.infrastructure.persistence.repository.AvailableSlotRepository;
import com.ali.reservation.infrastructure.persistence.repository.ReservationRepository;
import com.ali.reservation.infrastructure.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
class TestDataFactory {

    private final AvailableSlotRepository slotRepository;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;

    public void cleanDatabase() {
        reservationRepository.deleteAll();
        slotRepository.deleteAll();
        userRepository.deleteAll();
    }

    public TestUser createTestUser(String username) {
        var user = new UserEntity();
        user.setUsername(username + "-" + System.currentTimeMillis());
        user.setEmail(username + "-" + System.currentTimeMillis() + "@example.com");
        user.setPassword("secret");
        UserEntity saved = userRepository.saveAndFlush(user);
        return new TestUser(saved.getId(), saved.getUsername());
    }

    public AvailableSlotEntity createAvailableSlot(LocalDateTime startTime) {
        return createSlot(startTime, false);
    }

    public AvailableSlotEntity createReservedSlot(LocalDateTime startTime) {
        return createSlot(startTime, true);
    }

    private AvailableSlotEntity createSlot(LocalDateTime startTime, boolean reserved) {
        LocalDateTime endTime = startTime.plusHours(1);
        AvailableSlotEntity slot = new AvailableSlotEntity(startTime, endTime, reserved, null);
        return slotRepository.saveAndFlush(slot);
    }

    public void createReservation(Long userId, AvailableSlotEntity slot) {
        ReservationEntity reservation = ReservationEntity.of(userId, slot);
        reservationRepository.saveAndFlush(reservation);
    }

    public AvailableSlotEntity findSlotById(Long slotId) {
        return slotRepository.findById(slotId)
                .orElseThrow(() -> new AssertionError("Slot should exist: " + slotId));
    }

    public boolean reservationExists(Long userId, Long slotId) {
        return reservationRepository.findByUserIdAndSlotId(userId, slotId).isPresent();
    }
}
