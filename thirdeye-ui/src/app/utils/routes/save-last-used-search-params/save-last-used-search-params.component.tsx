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
