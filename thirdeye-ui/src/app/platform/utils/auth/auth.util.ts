// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
import { AuthExceptionCodeV1 } from "../../components/auth-provider-v1/auth-provider-v1.interfaces";

export const isBlockingAuthExceptionV1 = (
    authExceptionCode: AuthExceptionCodeV1
): boolean => {
    return (
        authExceptionCode &&
        authExceptionCode !== AuthExceptionCodeV1.UnauthorizedAccess
    );
};

export const AuthExceptionCodeV1Label: Record<AuthExceptionCodeV1, string> =
    Object.freeze({
        "001": "Application initialization failure | CODE 001",
        "002": "Initialization failure | CODE 002",
        "003": "Information call failure | CODE 003",
        "004": "Open ID configuration call failure | CODE 004",
        "005": "Information missing | CODE 005",
        "006": "Open ID configuration missing | CODE 006",
        "007": "Unauthorized access | CODE 007",
    });
