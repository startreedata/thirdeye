// Copyright 2022 StarTree Inc

// Licensed under the StarTree Community License (the "License"); you may not use
// this file except in compliance with the License. You may obtain a copy of the
// License at http://www.startree.ai/legal/startree-community-license

// Unless required by applicable law or agreed to in writing, software distributed under the
// License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
// either express or implied.
// See the License for the specific language governing permissions and limitations under
// the License.
import React, { FunctionComponent, useEffect } from "react";
import { useLocation, useSearchParams } from "react-router-dom";
import { useLastUsedSearchParams } from "../../../stores/last-used-params/last-used-search-params.store";
import { SaveLastUsedSearchParamsProps } from "./save-last-used-search-params.interfaces";

/**
 * Determine the key to use for the search query string by either using the
 * provided `pathKeyOverride` value or the location's pathname property
 *
 * @param pathKeyOverride - Use this value as the key if provided
 */
export const SaveLastUsedSearchParams: FunctionComponent<SaveLastUsedSearchParamsProps> =
    ({ pathKeyOverride, children }) => {
        const location = useLocation();
        const { setLastUsedForPath } = useLastUsedSearchParams();
        const [searchParams] = useSearchParams();
        const pathKey = pathKeyOverride || location.pathname;

        useEffect(() => {
            setLastUsedForPath(pathKey, searchParams.toString());
        }, [searchParams]);

        return <>{children}</>;
    };
