import React, { FunctionComponent, useEffect, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { RedirectValidationProps } from "./redirect-validation.interfaces";

/**
 * Validates they keys in `queryParams` exist in the searchParams and redirects
 * to the `to` property if not all the query params exist
 *
 * @param {string} to - Path to redirect to
 * @param {string[]} queryParams - Keys to check if they exist in the search params
 * @param {boolean} replace - Indicates to replace the history entry with the new path
 */
export const RedirectValidation: FunctionComponent<RedirectValidationProps> = ({
    to,
    queryParams,
    replace = true,
    ...props
}) => {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const [isValid, setIsValid] = useState(false);

    useEffect(() => {
        let validSoFar = true;

        queryParams.forEach((queryParamKey) => {
            validSoFar = validSoFar && searchParams.has(queryParamKey);
        });

        if (!validSoFar) {
            navigate(to, { replace });
        }

        setIsValid(validSoFar);
    }, [searchParams]);

    if (isValid) {
        return <>{props.children}</>;
    }

    return <></>;
};
