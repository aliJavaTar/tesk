package com.ali.reservation.usecase;

import com.ali.reservation.infrastructure.persistence.entity.AvailableSlotEntity;
import com.ali.reservation.infrastructure.persistence.entity.UserEntity;
import com.ali.reservation.infrastructure.persistence.repository.AvailableSlotRepository;
import com.ali.reservation.infrastructure.persistence.repository.ReservationRepository;
import com.ali.reservation.infrastructure.persistence.repository.UserRepository;
import com.ali.reservation.infrastructure.security.SecurityUtils;
import com.ali.reservation.presentation.dto.reqeust.ReserveSlotRequest;
import com.ali.reservation.presentation.exption.ApplicationException;
import lombok.RequiredArgsConstructor;
import org.mockito.MockedStatic;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.Mockito.mockStatic;

@Component
@RequiredArgsConstructor
class ConcurrencyTestHelper {
    
    private final AvailableSlotRepository slotRepository;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final ReserveTicketService reserveTicketService;

    @Transactional
    public void cleanDatabase() {
        reservationRepository.deleteAll();
        slotRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Transactional
    public AvailableSlotEntity createSlotForConcurrencyTest(LocalDateTime startTime) {
        LocalDateTime endTime = startTime.plusHours(1);
        AvailableSlotEntity slot = new AvailableSlotEntity(startTime, endTime, false, null);
        return slotRepository.saveAndFlush(slot);
    }

    @Transactional
    public TestUser createUserForConcurrencyTest(String username) {
        var user = new UserEntity();
        user.setUsername(username + "-concurrent-" + System.currentTimeMillis());
        user.setEmail(username + "-concurrent-" + System.currentTimeMillis() + "@example.com");
        user.setPassword("secret");
        UserEntity saved = userRepository.saveAndFlush(user);
        return new TestUser(saved.getId(), saved.getUsername());
    }

    public ConcurrencyTestResult executeSimultaneousReservations(
            AvailableSlotEntity slot, TestUser user1, TestUser user2) {
        
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completeLatch = new CountDownLatch(2);
        
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        // Submit both reservation tasks
        executor.submit(createReservationTask(slot, user1, startLatch, completeLatch, successCount, failureCount));
        executor.submit(createReservationTask(slot, user2, startLatch, completeLatch, successCount, failureCount));

        try {
            // Start both threads simultaneously
            startLatch.countDown();
            
            // Wait for both to complete
            boolean finished = completeLatch.await(10, TimeUnit.SECONDS);
            if (!finished) {
                throw new AssertionError("Concurrent reservation test timed out");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError("Test was interrupted", e);
        } finally {
            shutdownExecutor(executor);
        }

        return new ConcurrencyTestResult(successCount.get(), failureCount.get());
    }

    public ConcurrencyTestResult executeParallelReservations(
            TestUser user1, AvailableSlotEntity slot1, 
            TestUser user2, AvailableSlotEntity slot2) {
        
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completeLatch = new CountDownLatch(2);
        
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        // Submit reservation tasks for different slots
        executor.submit(createReservationTask(slot1, user1, startLatch, completeLatch, successCount, failureCount));
        executor.submit(createReservationTask(slot2, user2, startLatch, completeLatch, successCount, failureCount));

        try {
            // Start both threads simultaneously
            startLatch.countDown();
            
            // Wait for both to complete
            boolean finished = completeLatch.await(10, TimeUnit.SECONDS);
            if (!finished) {
                throw new AssertionError("Parallel reservation test timed out");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError("Test was interrupted", e);
        } finally {
            shutdownExecutor(executor);
        }

        return new ConcurrencyTestResult(successCount.get(), failureCount.get());
    }

    public AvailableSlotEntity findSlotById(Long slotId) {
        return slotRepository.findById(slotId)
                .orElseThrow(() -> new AssertionError("Slot should exist: " + slotId));
    }

    public long countReservationsForSlot(Long slotId) {
        return reservationRepository.findAll().stream()
                .filter(reservation -> reservation.getSlot().getId().equals(slotId))
                .count();
    }

    private Runnable createReservationTask(
            AvailableSlotEntity slot, 
            TestUser user, 
            CountDownLatch startLatch,
            CountDownLatch completeLatch,
            AtomicInteger successCount, 
            AtomicInteger failureCount) {
        
        return () -> {
            try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
                mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(user.id());
                
                // Wait for start signal
                startLatch.await();
                
                ReserveSlotRequest request = new ReserveSlotRequest();
                request.setSlotStartTime(slot.getStartTime());
                
                reserveTicketService.reserveSlot(request);
                successCount.incrementAndGet();
                
            } catch (ApplicationException | InterruptedException e) {
                failureCount.incrementAndGet();
            } catch (Exception e) {
                // Unexpected exceptions should fail the test
                throw new AssertionError("Unexpected exception in concurrent test", e);
            } finally {
                completeLatch.countDown();
            }
        };
    }

    private void shutdownExecutor(ExecutorService executor) {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}