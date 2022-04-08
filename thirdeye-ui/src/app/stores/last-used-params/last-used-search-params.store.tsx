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
