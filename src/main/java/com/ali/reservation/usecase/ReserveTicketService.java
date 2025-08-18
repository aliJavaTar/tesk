package com.ali.reservation.usecase;

import com.ali.reservation.infrastructure.persistence.entity.AvailableSlotEntity;
import com.ali.reservation.infrastructure.persistence.entity.ReservationEntity;
import com.ali.reservation.infrastructure.persistence.repository.AvailableSlotRepository;
import com.ali.reservation.infrastructure.persistence.repository.ReservationRepository;
import com.ali.reservation.presentation.dto.reqeust.ReserveSlotRequest;
import com.ali.reservation.presentation.dto.response.ReservationResponse;
import com.ali.reservation.presentation.exption.ApplicationException;
import com.ali.reservation.presentation.exption.EntityNotFountException;
import com.ali.reservation.presentation.exption.TimeNotValidException;
import com.ali.reservation.presentation.mapper.ReservationMapper;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.ali.reservation.infrastructure.security.SecurityUtils.*;
import static com.ali.reservation.presentation.exption.ErrorType.*;
import static java.time.LocalDateTime.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ReserveTicketService {

    private final AvailableSlotRepository repository;
    private final ReservationRepository reservationRepository;
    private final ReservationMapper reservationMapper;

    @Cacheable(cacheNames = "slots", key = "{#from, #pageable.pageNumber, #pageable.pageSize}")
    public Page<ReservationResponse> getAvailableSlots(LocalDateTime from, Pageable pageable) {
        log.info("Executing getAvailableSlots — not from cache");

        Optional.ofNullable(from)
                .filter(time -> time.isAfter(now()))
                .orElseThrow(() -> new TimeNotValidException(VALIDATION_ERROR, "Time must be in the present or future."));
        return repository.findAvailableSlots(from, pageable).map(reservationMapper::mapTo);
    }


    @CacheEvict(cacheNames = "slots", allEntries = true)
    @Retryable(
            retryFor = {ObjectOptimisticLockingFailureException.class, OptimisticLockException.class},
            notRecoverable = {EntityNotFountException.class},
            maxAttempts = 2,
            backoff = @Backoff(delay = 200)
    )
    @Transactional
    public void reserveSlot(ReserveSlotRequest reservationRequest) {
        AvailableSlotEntity availableSlotEntity = markSlotToReserved(reservationRequest.getSlotStartTime());
        repository.saveAndFlush(availableSlotEntity);
        reservationRepository.save(ReservationEntity.of(getCurrentUserId(), availableSlotEntity));
    }


    @CacheEvict(cacheNames = "slots", allEntries = true)
    @Transactional
    public void cancelReservation(Long slotId) {
        reservationRepository.findByUserIdAndSlotId(getCurrentUserId(), slotId)
                .ifPresentOrElse(this::markToCancel, () -> {
                    throw new EntityNotFountException(ENTITY_NOT_FOUND);
                });
    }

    private AvailableSlotEntity markSlotToReserved(LocalDateTime slotStartTime) {
        return repository.findByStartTime(slotStartTime).map(availableSlot -> {
            availableSlot.setReserved(true);
            return availableSlot;
        }).orElseThrow(() -> new EntityNotFountException(ENTITY_NOT_FOUND));
    }

    private void markToCancel(ReservationEntity reservationEntity) {
        AvailableSlotEntity availableSlotEntity = reservationEntity.getSlot();
        availableSlotEntity.setReserved(false);
        repository.save(availableSlotEntity);
        reservationRepository.delete(reservationEntity);
    }

    @Recover
    public void recoverFromOptimisticLock(ObjectOptimisticLockingFailureException ex, ReserveSlotRequest request) {
        log.error("Failed to reserve slot after retries: {}", request.getSlotStartTime(), ex);
        throw new ApplicationException(CONFLICT, "Could not reserve slot after retries");
    }

    @Recover
    public void recoverFromOptimisticLock(OptimisticLockException ex, ReserveSlotRequest request) {
        log.error("Failed to reserve slot after retries: {}", request.getSlotStartTime(), ex);
        throw new ApplicationException(CONFLICT, "Could not reserve slot after retries");
    }
}
