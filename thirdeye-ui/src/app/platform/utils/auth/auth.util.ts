/*
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
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
