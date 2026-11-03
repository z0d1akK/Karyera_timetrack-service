package com.zodiakk.timetrackservice.schedule.dto.request.update;

import com.zodiakk.timetrackservice.common.validation.ValidationMessages;

import io.swagger.v3.oas.annotations.media.Schema;
import com.zodiakk.timetrackservice.schedule.dto.request.create.WorkScheduleDayInputDto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkScheduleUpdateRequestDto {

    @Schema(description = "Code")
    @Size(max = 50, message = ValidationMessages.CODE_SIZE)
    private String code;

    @Schema(description = "Name")
    @Size(max = 150, message = ValidationMessages.NAME_SIZE)
    private String name;

    @Schema(description = "Description")
    private String description;

    @Schema(description = "Timezone")
    @Size(max = 100, message = ValidationMessages.TIMEZONE_SIZE)
    private String timezone;

    @Schema(description = "Active")
    private Boolean active;

    @Schema(description = "Replacement of the weekly template. Omitted value keeps the current days")
    @Valid
    @Size(max = 7, message = ValidationMessages.SCHEDULE_DAYS_SIZE)
    private List<WorkScheduleDayInputDto> days;
}
