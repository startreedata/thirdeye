// Copyright 2022 StarTree Inc

// Licensed under the StarTree Community License (the "License"); you may not use
// this file except in compliance with the License. You may obtain a copy of the
// License at http://www.startree.ai/legal/startree-community-license

// Unless required by applicable law or agreed to in writing, software distributed under the
// License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
// either express or implied.
// See the License for the specific language governing permissions and limitations under
// the License.
import create from "zustand";
import { LastUsedSearchParamsStore } from "./last-used-search-params.interfaces";

/**
 * Simple key value store whose keys are expected to be path names and values
 * are query search strings "/hello/world": "param1=value1&param2=value2"
 */
export const useLastUsedSearchParams = create<LastUsedSearchParamsStore>(
    (set, get) => ({
        pathsToQueryStrings: {},

        reset: () => {
            set({
                pathsToQueryStrings: {},
            });
        },

        setLastUsedForPath: (path: string, searchString: string) => {
            if (searchString === "") {
                return;
            }
            const { pathsToQueryStrings } = get();
            set({
                pathsToQueryStrings: {
                    ...pathsToQueryStrings,
                    [path]: searchString,
                },
            });
        },

        getLastUsedForPath: (path: string) => {
            const { pathsToQueryStrings } = get();

            return pathsToQueryStrings[path];
        },
    })
);
