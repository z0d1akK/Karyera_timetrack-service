package com.zodiakk.timetrackservice.common.exception;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ErrorMessages {

    public static final String CONCURRENT_MODIFICATION = "Объект был изменён другим пользователем. Обновите данные и повторите попытку";

    public static final String INTERNAL_SERVER_ERROR = "Внутренняя ошибка сервера. Попробуйте позднее.";

    public static final String DATA_INTEGRITY_VIOLATION = "Нарушено ограничение целостности данных";

    public static final String INVALID_REQUEST_BODY = "Некорректное тело запроса";

    public static final String INVALID_PARAMETER = "Некорректное значение параметра: %s";

    public static final String MISSING_PARAMETER = "Обязательный параметр отсутствует: %s";

    public static final String PAYLOAD_TOO_LARGE = "Размер загружаемого файла превышает допустимый";

    public static final String UNSUPPORTED_MEDIA_TYPE = "Неподдерживаемый тип содержимого запроса";

    public static final String ACCESS_DENIED = "Доступ запрещён";

    public static final String UNAUTHORIZED = "Требуется аутентификация";

    public static final String INVALID_INTERNAL_API_KEY = "Некорректный внутренний API-ключ";

    public static final String WORK_SCHEDULE_NOT_FOUND = "Рабочий график не найден: %s";

    public static final String TIME_ENTRY_NOT_FOUND = "Запись рабочего времени не найдена: %s";

    public static final String TIMESHEET_NOT_FOUND = "Табель не найден: %s";

    public static final String EMPLOYEE_PROFILE_NOT_FOUND = "Профиль сотрудника не найден для пользователя: %s";

    public static final String EMPLOYEE_NOT_FOUND = "Сотрудник не найден: %s";

    public static final String PROJECT_SERVICE_UNAVAILABLE = "Сервис проектов временно недоступен. Повторите операцию";

    public static final String EMPLOYEE_SERVICE_UNAVAILABLE = "Сервис сотрудников временно недоступен. Повторите операцию";

    public static final String OUTBOX_EVENT_SERIALIZATION_FAILED = "Не удалось сериализовать событие %s";
}
