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
import React, { FunctionComponent, useLayoutEffect, useState } from "react";
import { useLocation } from "react-router-dom";
import type { CancelNetworkCallsContextProps } from "./cancel-network-calls-wrapper.interfaces";
import {
    CancelNetworkCallsContext,
    getNewCancelToken,
} from "./cancel-network-calls-wrapper.utils";

export const CancelNetworkCallsWrapper: FunctionComponent = ({ children }) => {
    const [interceptorId, setInterceptorId] = useState<number | null>();
    const location = useLocation();

    const [cancelTokenSource, setCancelTokenSource] =
        useState<CancelTokenSource>(getNewCancelToken());

    const cancelApiCalls = (): void => {
        cancelTokenSource.cancel("cancelApiCalls invoked");
        setCancelTokenSource(getNewCancelToken());
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
        return () => {
            cancelApiCalls();
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
