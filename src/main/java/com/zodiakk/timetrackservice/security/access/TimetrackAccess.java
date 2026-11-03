package com.zodiakk.timetrackservice.security.access;

import com.zodiakk.timetrackservice.common.constants.SecurityConstants;
import com.zodiakk.timetrackservice.integration.employee.service.EmployeeDirectoryService;
import com.zodiakk.timetrackservice.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Component("timetrackAccess")
@RequiredArgsConstructor
public class TimetrackAccess {

    private final EmployeeDirectoryService employeeDirectory;

    public UUID currentEmployeeId() {
        return employeeDirectory.currentEmployeeId();
    }

    public boolean isHrOrAdmin() {
        return SecurityUtils.hasAnyRole(
                SecurityConstants.HR_AUTHORITY,
                SecurityConstants.ADMIN_AUTHORITY
        );
    }

    public boolean isManager() {
        return SecurityUtils.hasRole(SecurityConstants.MANAGER_AUTHORITY);
    }

    public void requireHrOrAdmin() {
        if (!isHrOrAdmin()) {
            throw new AccessDeniedException("Недостаточно прав для управления рабочими графиками");
        }
    }

    public void requireCanView(UUID employeeId) {
        if (isHrOrAdmin()) {
            return;
        }

        UUID actorEmployeeId = currentEmployeeId();

        if (actorEmployeeId.equals(employeeId)) {
            return;
        }

        if (employeeDirectory.getAccessScope(employeeId, actorEmployeeId).managerOfEmployee()) {
            return;
        }

        throw new AccessDeniedException("Нет доступа к данным выбранного сотрудника");
    }

    public void requireCanApprove(UUID employeeId) {
        UUID actorEmployeeId = currentEmployeeId();

        if (actorEmployeeId.equals(employeeId)) {
            throw new AccessDeniedException("Нельзя согласовать собственный табель");
        }

        if (isHrOrAdmin()) {
            return;
        }

        if (employeeDirectory.getAccessScope(employeeId, actorEmployeeId).managerOfEmployee()) {
            return;
        }

        throw new AccessDeniedException("Табель утверждает руководитель сотрудника, HR или администратор");
    }

    public void requireCanReviewQueue() {
        if (isHrOrAdmin() || isManager()) {
            return;
        }

        throw new AccessDeniedException("Очередь согласования доступна руководителю, HR или администратору");
    }

    public void requireCanViewProjectSummary() {
        if (isHrOrAdmin() || isManager()) {
            return;
        }

        throw new AccessDeniedException("Сводка по проекту доступна руководителю, HR или администратору");
    }

    /**
     * @return employee ids the caller may see, or {@code null} when the caller is HR or ADMIN
     */
    public Set<UUID> visibleEmployeeIds() {
        if (isHrOrAdmin()) {
            return null;
        }

        UUID self = currentEmployeeId();

        if (isManager()) {
            Set<UUID> visible = new LinkedHashSet<>(employeeDirectory.subordinateIds(self));
            visible.add(self);
            return visible;
        }

        return Set.of(self);
    }
}
