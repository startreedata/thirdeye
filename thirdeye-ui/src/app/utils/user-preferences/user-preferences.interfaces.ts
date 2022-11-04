export enum UserPreferencesKeys {
    SHOW_DOCUMENTATION_RESOURCES = "showHomeDocumentationResources",
}

export interface UserPreferences {
    showHomeDocumentationResources?: boolean;
}

export interface UseUserPreferencesHook {
    setPreference: (
        key: keyof UserPreferences,
        value: string | boolean
    ) => void;
    getPreference: (
        key: keyof UserPreferences
    ) => string | boolean | null | undefined;
    localPreferences: UserPreferences;
}
