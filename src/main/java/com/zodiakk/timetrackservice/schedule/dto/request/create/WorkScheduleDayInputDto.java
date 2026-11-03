package com.zodiakk.timetrackservice.schedule.dto.request.create;

import com.zodiakk.timetrackservice.common.validation.ValidationMessages;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkScheduleDayInputDto {

    @Schema(description = "ISO day of week, Monday is 1")
    @NotNull(message = ValidationMessages.DAY_OF_WEEK_REQUIRED)
    @Min(value = 1, message = "День недели должен быть от 1 до 7")
    @Max(value = 7, message = "День недели должен быть от 1 до 7")
    private Short dayOfWeek;

    @Schema(description = "Planned minutes")
    @NotNull(message = ValidationMessages.PLANNED_MINUTES_REQUIRED)
    @Min(value = 0, message = "Плановые минуты должны быть от 0 до 1440")
    @Max(value = 1440, message = "Плановые минуты должны быть от 0 до 1440")
    private Integer plannedMinutes;

    @Schema(description = "Reference start time")
    private LocalTime startTime;

    @Schema(description = "Reference end time")
    private LocalTime endTime;
}
