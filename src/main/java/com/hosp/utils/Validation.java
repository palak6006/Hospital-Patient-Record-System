package com.hosp.utils;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class Validation 
{
    public static LocalDate parseDate(String dateStr) throws DataFormatException 
    {
        try {
            return LocalDate.parse(dateStr); // yyyy-MM-dd
        } catch (DateTimeParseException e) {
            throw new DataFormatException("Invalid date format: use YYYY-MM-DD", e);
        }
    }

    public static void validateAge(int age) throws AppException 
    {
        if (age <= 0 || age > 150) throw new AppException("Invalid age: " + age);
    }

    public static void requireNonNull(Object o, String name) throws AppException 
    {
        if (o == null) throw new AppException(name + " cannot be null");
    }
}