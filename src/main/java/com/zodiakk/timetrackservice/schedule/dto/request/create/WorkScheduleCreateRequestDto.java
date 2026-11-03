package com.zodiakk.timetrackservice.schedule.dto.request.create;

import com.zodiakk.timetrackservice.common.validation.ValidationMessages;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkScheduleCreateRequestDto {

    @Schema(description = "Code")
    @NotBlank(message = ValidationMessages.CODE_REQUIRED)
    @Size(max = 50, message = ValidationMessages.CODE_SIZE)
    private String code;

    @Schema(description = "Name")
    @NotBlank(message = ValidationMessages.NAME_REQUIRED)
    @Size(max = 150, message = ValidationMessages.NAME_SIZE)
    private String name;

    @Schema(description = "Description")
    private String description;

    @Schema(description = "Timezone")
    @NotBlank(message = ValidationMessages.TIMEZONE_REQUIRED)
    @Size(max = 100, message = ValidationMessages.TIMEZONE_SIZE)
    private String timezone;

    @Schema(description = "Active")
    private Boolean active;

    @Schema(description = "Days of the weekly template")
    @Valid
    @Size(max = 7, message = ValidationMessages.SCHEDULE_DAYS_SIZE)
    private List<WorkScheduleDayInputDto> days;
}
