package com.ali.reservation.presentation.dto.reqeust;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReserveSlotRequest {
    @Min(1)
    @NotNull(message = "Slot ID is required")
    private Long slotId;
}
