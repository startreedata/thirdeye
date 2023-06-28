/*
 * Copyright 2023 StarTree Inc
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
import React, { FunctionComponent, useEffect, useState } from "react";
import {
    Outlet,
    useNavigate,
    useOutletContext,
    useSearchParams,
} from "react-router-dom";
import { RedirectValidationProps } from "./redirect-validation.interfaces";

/**
 * Validates they keys in `queryParams` exist in the searchParams and redirects
 * to the `to` property if not all the query params exist
 *
 * @param {string} to - Path to redirect to
 * @param {string[]} queryParams - Keys to check if they exist in the search params
 * @param {boolean} replace - Indicates to replace the history entry with the new path
 * @param {boolean} useOutlet - Indicates to return outlet component or not when valid
 */
export const RedirectValidation: FunctionComponent<RedirectValidationProps> = ({
    to,
    queryParams,
    replace = true,
    useOutlet,
    ...props
}) => {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const [isValid, setIsValid] = useState(false);
    const context = useOutletContext();

    useEffect(() => {
        let validSoFar = true;

        queryParams.forEach((queryParamKey) => {
            validSoFar = validSoFar && searchParams.has(queryParamKey);
        });

        if (!validSoFar) {
            let urlToNavigateTo = to;

            if (searchParams.toString()) {
                urlToNavigateTo += `?${searchParams.toString()}`;
            }
            navigate(urlToNavigateTo, { replace });
        }

        setIsValid(validSoFar);
    }, [searchParams]);

    if (isValid) {
        if (useOutlet) {
            return (
                <>
                    {props.children}
                    <Outlet context={context} />
                </>
            );
        }

        return <>{props.children}</>;
    }

    return <></>;
};
