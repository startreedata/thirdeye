///
/// Copyright 2022 StarTree Inc
///
/// Licensed under the StarTree Community License (the "License"); you may not use
/// this file except in compliance with the License. You may obtain a copy of the
/// License at http://www.startree.ai/legal/startree-community-license
///
/// Unless required by applicable law or agreed to in writing, software distributed under the
/// License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
/// either express or implied.
/// See the License for the specific language governing permissions and limitations under
/// the License.
///


// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
import { historyV1 } from "../history/history.util";

export const getQueryParamV1 = (key: string): string => {
    let value = "";
    const urlSearchParams = new URLSearchParams(location.search);
    if (urlSearchParams.has(key)) {
        value = urlSearchParams.get(key) as string;
    }

    return value;
};

export const setQueryParamV1 = (key: string, value: string): void => {
    if (!key) {
        return;
    }

    // Get existing query string
    const urlSearchParams = new URLSearchParams(location.search);
    urlSearchParams.set(key, value);

    // Update URL
    historyV1.replace({
        ...historyV1.location,
        search: urlSearchParams.toString(),
    });
};

export const clearQueryParamV1 = (key: string): void => {
    if (!key) {
        return;
    }

    // Get existing query string
    const urlSearchParams = new URLSearchParams(location.search);
    urlSearchParams.delete(key);

    // Update URL
    historyV1.replace({
        ...historyV1.location,
        search: urlSearchParams.toString(),
    });
};

export const getHashParamV1 = (key: string): string => {
    let value = "";
    const urlSearchParams = new URLSearchParams(location.hash.substr(1));
    if (urlSearchParams.has(key)) {
        value = urlSearchParams.get(key) as string;
    }

    return value;
};

export const setHashParamV1 = (key: string, value: string): void => {
    if (!key) {
        return;
    }

    // Get existing hash string
    const urlSearchParams = new URLSearchParams(location.hash.substr(1));
    urlSearchParams.set(key, value);

    // Update URL
    historyV1.replace({
        ...historyV1.location,
        hash: `#${urlSearchParams.toString()}`,
    });
};

export const clearHashParamV1 = (key: string): void => {
    if (!key) {
        return;
    }

    // Get existing hash string
    const urlSearchParams = new URLSearchParams(location.hash.substr(1));
    urlSearchParams.delete(key);

    // Update URL
    historyV1.replace({
        ...historyV1.location,
        hash: urlSearchParams.toString()
            ? `#${urlSearchParams.toString()}`
            : "",
    });
};
