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

import axios, { CancelTokenSource } from "axios";
import React, {
    FunctionComponent,
    useLayoutEffect,
    useRef,
    useState,
} from "react";
import { useLocation } from "react-router-dom";
import type { CancelNetworkCallsContextProps } from "./cancel-network-calls-wrapper.interfaces";
import {
    CancelNetworkCallsContext,
    getNewCancelToken,
} from "./cancel-network-calls-wrapper.utils";

export const CancelNetworkCallsWrapper: FunctionComponent = ({ children }) => {
    const [interceptorId, setInterceptorId] = useState<number | null>();
    const location = useLocation();
    const previousPath = useRef<string>(location.pathname); // Will always have the previous value

    const [cancelTokenSource, setCancelTokenSource] =
        useState<CancelTokenSource>(getNewCancelToken());

    const reloadCancelToken = (): void => {
        setCancelTokenSource(getNewCancelToken());
    };

    const cancelApiCalls = (): void => {
        cancelTokenSource.cancel("cancelApiCalls invoked");
        reloadCancelToken();
    };

    useLayoutEffect(() => {
        interceptorId && axios.interceptors.request.eject(interceptorId);

        const newInterceptorId = axios.interceptors.request.use((config) => ({
            ...config,
            cancelToken: cancelTokenSource.token,
        }));

        setInterceptorId(newInterceptorId);
    }, [cancelTokenSource]);

    useLayoutEffect(() => {
        // Do not Cancel API calls on redirect IF new path is a child of the existing path
        // (eg: /alerts/all to /alerts/all/stats). This would also cover the case for same path
        const shouldCancel = !location.pathname.startsWith(
            previousPath.current
        );

        previousPath.current = location.pathname;

        // TODO: Remove
        axios.interceptors.request.use((c) => {
            // console.log(
            //     `[shouldCancel: ${shouldCancel}] API: ${c.url}\n
            // From: ${previousPath.current}\nTo:   ${location.pathname}`
            // );

            return c;
        });

        return () => {
            shouldCancel ? cancelApiCalls() : reloadCancelToken();
        };
    }, [location.pathname]);

    const cancelNetworkCallsContext: CancelNetworkCallsContextProps = {
        cancelApiCalls,
        cancelTokenSource,
    };

    return (
        <CancelNetworkCallsContext.Provider value={cancelNetworkCallsContext}>
            {children}
        </CancelNetworkCallsContext.Provider>
    );
};
