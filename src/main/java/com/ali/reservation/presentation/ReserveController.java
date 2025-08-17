package com.ali.reservation.presentation;

import com.ali.reservation.presentation.dto.reqeust.ReserveSlotRequest;
import com.ali.reservation.presentation.dto.response.ReservationResponse;
import com.ali.reservation.usecase.ReserveTicketService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
public class ReserveController {

    private final ReserveTicketService reserveTicketService;


    @GetMapping("/slots")
    public Page<ReservationResponse> getAvailableSlots(
            @Parameter(
                    name = "from",
                    description = "Start datetime in ISO format: `yyyy-MM-dd'T'HH:mm:ss`",
                    required = true,
                    schema = @Schema(type = "string", format = "date-time", example = "2025-12-29T09:00:00")
            )
            @RequestParam(name = "from")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime from,
            @ParameterObject @PageableDefault Pageable pageable) {
        return reserveTicketService.getAvailableSlots(from, pageable);
    }

    @PostMapping
    public void reserveSlot(@RequestBody ReserveSlotRequest slotRequest) {
        reserveTicketService.reserveSlot(slotRequest);
    }

    @DeleteMapping("/{id}")
    public void cancelReservation(@PathVariable Long id) {
        reserveTicketService.cancelReservation(id);
    }
}
