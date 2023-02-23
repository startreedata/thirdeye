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

import axios from "axios";
import React, { FunctionComponent, useEffect, useState } from "react";
import { CancelAPICallsOnPageUnloadProps } from "./cancel-api-calls-on-page-unload.interfaces";

/**
 * This has to be wrapped around a page and not a router.
 * The `key` property needs to be set so that this component is unique when
 * used in react router: https://stackoverflow.com/questions/38710250/react-multiple
 * -instances-of-same-component-are-getting-same-state
 */
export const CancelAPICallsOnPageUnload: FunctionComponent<CancelAPICallsOnPageUnloadProps> =
    ({ children }) => {
        const [controller] = useState(new AbortController());
        axios.defaults.signal = controller.signal;

        useEffect(() => {
            axios.defaults.signal = controller.signal;

            return () => {
                controller.abort();
                axios.defaults.signal = undefined;
            };
        }, []);

        return <>{children}</>;
    };
