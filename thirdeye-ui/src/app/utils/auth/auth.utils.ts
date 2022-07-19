import { AuthExceptionCodeV1 } from "@startree-ui/platform-ui";

export const AuthExceptionCodeV1Label: Record<AuthExceptionCodeV1, string> =
    Object.freeze({
        app_init_failure: "Application initialization failure | CODE 001",
        init_failure: "Initialization failure | CODE 002",
        info_call_failure: "Information call failure | CODE 003",
        openid_configuration_call_failure:
            "Open ID configuration call failure | CODE 004",
        info_missing: "Information missing | CODE 005",
        openid_configuration_missing:
            "Open ID configuration missing | CODE 006",
        unauthorized_access: "Unauthorized access | CODE 007",
        access_denied: "Access denied | CODE 008",
    });
