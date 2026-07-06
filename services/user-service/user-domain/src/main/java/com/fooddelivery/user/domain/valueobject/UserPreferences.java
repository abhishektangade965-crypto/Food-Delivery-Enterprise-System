package com.fooddelivery.user.domain.valueobject;

import java.util.List;
import java.util.UUID;

public record UserPreferences(
        List<String> defaultCuisines,
        List<String> dietaryRestrictions,
        UUID defaultPaymentMethodId,
        boolean notificationsEnabled,
        boolean emailNotifications,
        boolean smsNotifications,
        boolean pushNotifications,
        String language,
        String currency
) {
    public static UserPreferences defaultPreferences() {
        return new UserPreferences(
                List.of(),
                List.of(),
                null,
                true,
                true,
                false,
                true,
                "en",
                "USD"
        );
    }

    public UserPreferences withLanguage(String language) {
        return new UserPreferences(defaultCuisines, dietaryRestrictions, defaultPaymentMethodId,
                notificationsEnabled, emailNotifications, smsNotifications, pushNotifications, language, currency);
    }

    public UserPreferences withCurrency(String currency) {
        return new UserPreferences(defaultCuisines, dietaryRestrictions, defaultPaymentMethodId,
                notificationsEnabled, emailNotifications, smsNotifications, pushNotifications, language, currency);
    }
}
