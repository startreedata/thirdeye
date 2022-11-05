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
import { AxiosError, AxiosRequestConfig } from "axios";

// Returns axios request interceptor
export const getRequestInterceptorV1 = (
    accessToken: string
): ((requestConfig: AxiosRequestConfig) => AxiosRequestConfig) => {
    const requestInterceptor = (
        requestConfig: AxiosRequestConfig
    ): AxiosRequestConfig => {
        // If access token is available, attach it to the request
        if (accessToken) {
            requestConfig.headers = {
                Authorization: `Bearer ${accessToken}`,
            };
        }

        return requestConfig;
    };

    return requestInterceptor;
};

// Returns axios fulfilled response interceptor
export const getFulfilledResponseInterceptorV1 = (): (<T>(
    response: T
) => T | Promise<T>) => {
    const fulfilledResponseInterceptor = <T>(response: T): T | Promise<T> => {
        // Forward the fulfilled response
        return response;
    };

    return fulfilledResponseInterceptor;
};

// Returns axios rejected response interceptor
export const getRejectedResponseInterceptorV1 = (
    unauthenticatedAccessFn: () => void
): ((error: AxiosError) => void) => {
    const rejectedResponseInterceptor = (error: AxiosError): void => {
        if (error && error.response && error.response.status === 401) {
            // Unauthenticated access
            unauthenticatedAccessFn && unauthenticatedAccessFn();
        }

        throw error;
    };

    return rejectedResponseInterceptor;
};

export const duplicateKeyForArrayQueryParams = (params: {
    [key: string]: number | string | string[] | boolean;
}): string => {
    const searchStringParts: string[] = [];

    Object.entries(params).forEach(([queryParamKey, value]) => {
        if (Array.isArray(value)) {
            value.forEach((val) => {
                searchStringParts.push(`${queryParamKey}=${val}`);
            });
        } else {
            searchStringParts.push(`${queryParamKey}=${value}`);
        }
    });

    return searchStringParts.length > 0 ? searchStringParts.join("&") : "";
};
