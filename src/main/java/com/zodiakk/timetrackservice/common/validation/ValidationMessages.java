package com.zodiakk.timetrackservice.common.validation;

public final class ValidationMessages {

    private ValidationMessages() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static final String ACTION_REQUIRED = "Действие обязательно для заполнения";

    public static final String CODE_REQUIRED = "Код обязателен для заполнения";

    public static final String CODE_SIZE = "Код должен содержать не более {max} символов";

    public static final String COMMENT_REQUIRED = "Комментарий обязателен для заполнения";

    public static final String COMMENT_SIZE = "Комментарий должен содержать не более {max} символов";

    public static final String DAY_OF_WEEK_REQUIRED = "День недели обязателен для заполнения";

    public static final String DECISION_REQUIRED = "Решение обязательно для заполнения";

    public static final String DURATION_MINUTES_REQUIRED = "Длительность в минутах обязательна для заполнения";

    public static final String EMPLOYEE_ID_REQUIRED = "Идентификатор сотрудника обязателен для заполнения";

    public static final String EXPECTED_VERSION_REQUIRED = "Ожидаемая версия обязательна для заполнения";

    public static final String NAME_REQUIRED = "Название обязательно для заполнения";

    public static final String NAME_SIZE = "Название должно содержать не более {max} символов";

    public static final String PAGE_MAX_SIZE = "Размер страницы не может превышать {value}";

    public static final String PAGE_MIN_SIZE = "Размер страницы должен быть больше нуля";

    public static final String PAGE_NUMBER_MIN = "Номер страницы не может быть отрицательным";

    public static final String PERIOD_END_REQUIRED = "Окончание периода обязательно для заполнения";

    public static final String PERIOD_START_REQUIRED = "Начало периода обязательно для заполнения";

    public static final String PLANNED_MINUTES_REQUIRED = "Количество плановых минут обязательно для заполнения";

    public static final String PROJECT_ID_REQUIRED = "Идентификатор проекта обязателен для заполнения";

    public static final String REASON_REQUIRED = "Причина обязательна для заполнения";

    public static final String REASON_SIZE = "Причина должна содержать не более {max} символов";

    public static final String SCHEDULE_DAYS_SIZE = "В графике не может быть больше {max} дней";

    public static final String SCHEDULE_ID_REQUIRED = "Идентификатор графика обязателен для заполнения";

    public static final String TASK_ID_REQUIRED = "Идентификатор задачи обязателен для заполнения";

    public static final String TIMESHEET_ID_REQUIRED = "Идентификатор табеля обязателен для заполнения";

    public static final String TIMEZONE_REQUIRED = "Часовой пояс обязателен для заполнения";

    public static final String TIMEZONE_SIZE = "Часовой пояс должен содержать не более {max} символов";

    public static final String TIME_ENTRY_ID_REQUIRED = "Идентификатор записи времени обязателен для заполнения";

    public static final String VALID_FROM_REQUIRED = "Дата начала действия обязательна для заполнения";

    public static final String WORK_DATE_REQUIRED = "Рабочая дата обязательна для заполнения";
}
