export interface LastUsedSearchParamsStore {
    pathsToQueryStrings: {
        [k: string]: string;
    };
    setLastUsedForPath: (path: string, searchString: string) => void;
    getLastUsedForPath: (path: string) => string | undefined;
    reset: () => void;
}
