package com.zodiakk.timetrackservice.schedule.dto.request.filter;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkScheduleFilterDto {

    @Schema(description = "Code")
    private String code;

    @Schema(description = "Name")
    private String name;

    @Schema(description = "Timezone")
    private String timezone;

    @Schema(description = "Active")
    private Boolean active;
}
