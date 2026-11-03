package com.zodiakk.timetrackservice.schedule.service.impl;

import com.zodiakk.timetrackservice.common.cache.CacheNames;
import com.zodiakk.timetrackservice.common.dto.PageRequestDto;
import com.zodiakk.timetrackservice.common.exception.ConflictException;
import com.zodiakk.timetrackservice.schedule.dto.request.create.WorkScheduleCreateRequestDto;
import com.zodiakk.timetrackservice.schedule.dto.request.create.WorkScheduleDayInputDto;
import com.zodiakk.timetrackservice.schedule.dto.request.filter.WorkScheduleFilterDto;
import com.zodiakk.timetrackservice.schedule.dto.request.update.WorkScheduleUpdateRequestDto;
import com.zodiakk.timetrackservice.schedule.dto.response.WorkScheduleDayResponseDto;
import com.zodiakk.timetrackservice.schedule.dto.response.WorkScheduleResponseDto;
import com.zodiakk.timetrackservice.schedule.entity.WorkSchedule;
import com.zodiakk.timetrackservice.schedule.entity.WorkScheduleDay;
import com.zodiakk.timetrackservice.schedule.exception.WorkScheduleNotFoundException;
import com.zodiakk.timetrackservice.schedule.mapper.WorkScheduleDayMapper;
import com.zodiakk.timetrackservice.schedule.mapper.WorkScheduleMapper;
import com.zodiakk.timetrackservice.schedule.repository.EmployeeWorkScheduleRepository;
import com.zodiakk.timetrackservice.schedule.repository.WorkScheduleDayRepository;
import com.zodiakk.timetrackservice.schedule.repository.WorkScheduleRepository;
import com.zodiakk.timetrackservice.schedule.repository.specification.workSchedule.WorkSchedulePageableFactory;
import com.zodiakk.timetrackservice.schedule.repository.specification.workSchedule.WorkScheduleSpecification;
import com.zodiakk.timetrackservice.schedule.service.WorkScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkScheduleServiceImpl implements WorkScheduleService {

    private final WorkScheduleRepository workScheduleRepository;

    private final WorkScheduleDayRepository workScheduleDayRepository;

    private final EmployeeWorkScheduleRepository employeeWorkScheduleRepository;

    private final WorkScheduleMapper workScheduleMapper;

    private final WorkScheduleDayMapper workScheduleDayMapper;

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    public WorkScheduleResponseDto create(WorkScheduleCreateRequestDto request) {
        String code = normalizeCode(request.getCode());
        requireUniqueCode(code, null);
        requireTimezone(request.getTimezone());
        requireDays(request.getDays());

        WorkSchedule schedule = workScheduleMapper.toEntity(request);
        schedule.setCode(code);
        schedule.setTimezone(request.getTimezone().trim());

        if (schedule.getActive() == null) {
            schedule.setActive(true);
        }

        WorkSchedule saved = workScheduleRepository.save(schedule);
        replaceDays(saved.getId(), request.getDays());
        return toResponse(saved);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    @CacheEvict(cacheNames = CacheNames.WORK_SCHEDULES, key = "#id")
    public WorkScheduleResponseDto update(UUID id, WorkScheduleUpdateRequestDto request) {
        WorkSchedule schedule = workScheduleRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new WorkScheduleNotFoundException(id));

        if (request.getCode() != null && !request.getCode().isBlank()) {
            String code = normalizeCode(request.getCode());
            requireUniqueCode(code, id);
            schedule.setCode(code);
        }

        if (request.getTimezone() != null && !request.getTimezone().isBlank()) {
            requireTimezone(request.getTimezone());
            schedule.setTimezone(request.getTimezone().trim());
        }

        if (request.getName() != null) {
            schedule.setName(request.getName());
        }

        if (request.getDescription() != null) {
            schedule.setDescription(request.getDescription());
        }

        if (request.getActive() != null) {
            schedule.setActive(request.getActive());
        }

        if (request.getDays() != null) {
            if (!employeeWorkScheduleRepository.findAllByScheduleId(id).isEmpty()) {
                throw new ConflictException(
                        "Состав дней уже назначенного графика менять нельзя. Создайте новый график и назначьте его сотрудникам"
                );
            }

            requireDays(request.getDays());
            replaceDays(id, request.getDays());
        }

        return toResponse(workScheduleRepository.save(schedule));
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    @Cacheable(cacheNames = CacheNames.WORK_SCHEDULES, key = "#id")
    public WorkScheduleResponseDto get(UUID id) {
        WorkSchedule schedule = workScheduleRepository.findById(id)
                .orElseThrow(() -> new WorkScheduleNotFoundException(id));
        return toResponse(schedule);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public Page<WorkScheduleResponseDto> findAll(WorkScheduleFilterDto filter, PageRequestDto pageRequest) {
        return workScheduleRepository.findAll(
                WorkScheduleSpecification.filter(filter),
                WorkSchedulePageableFactory.create(pageRequest)
        ).map(this::toResponse);
    }

    private void replaceDays(UUID scheduleId, List<WorkScheduleDayInputDto> days) {
        workScheduleDayRepository.deleteAllByScheduleId(scheduleId);
        workScheduleDayRepository.flush();

        if (days == null || days.isEmpty()) {
            return;
        }

        List<WorkScheduleDay> entities = new ArrayList<>();

        for (WorkScheduleDayInputDto day : days) {
            WorkScheduleDay entity = new WorkScheduleDay();
            entity.setScheduleId(scheduleId);
            entity.setDayOfWeek(day.getDayOfWeek());
            entity.setPlannedMinutes(day.getPlannedMinutes());
            entity.setStartTime(day.getStartTime());
            entity.setEndTime(day.getEndTime());
            entities.add(entity);
        }

        workScheduleDayRepository.saveAll(entities);
    }

    private void requireDays(List<WorkScheduleDayInputDto> days) {
        if (days == null || days.isEmpty()) {
            return;
        }

        Set<Short> seen = new HashSet<>();

        for (WorkScheduleDayInputDto day : days) {
            if (!seen.add(day.getDayOfWeek())) {
                throw new ConflictException("День недели %s указан в графике повторно".formatted(day.getDayOfWeek()));
            }

            LocalTime start = day.getStartTime();
            LocalTime end = day.getEndTime();

            if (start != null && end != null && !end.isAfter(start)) {
                throw new ConflictException("Время окончания дня должно быть позже времени начала");
            }
        }
    }

    private void requireUniqueCode(String code, UUID currentId) {
        boolean exists = currentId == null
                ? workScheduleRepository.existsByCodeIgnoreCase(code)
                : workScheduleRepository.existsByCodeIgnoreCaseAndIdNot(code, currentId);

        if (exists) {
            throw new ConflictException("График с кодом %s уже существует".formatted(code));
        }
    }

    private void requireTimezone(String timezone) {
        if (timezone == null || !ZoneId.getAvailableZoneIds().contains(timezone.trim())) {
            throw new ConflictException("Часовой пояс должен быть идентификатором IANA");
        }
    }

    private String normalizeCode(String code) {
        return code == null ? null : code.trim();
    }

    private WorkScheduleResponseDto toResponse(WorkSchedule schedule) {
        WorkScheduleResponseDto response = workScheduleMapper.toResponseDto(schedule);
        List<WorkScheduleDayResponseDto> days = workScheduleDayRepository
                .findAllByScheduleIdOrderByDayOfWeekAsc(schedule.getId())
                .stream()
                .map(workScheduleDayMapper::toResponseDto)
                .toList();
        response.setDays(days);
        return response;
    }
}
