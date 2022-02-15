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
