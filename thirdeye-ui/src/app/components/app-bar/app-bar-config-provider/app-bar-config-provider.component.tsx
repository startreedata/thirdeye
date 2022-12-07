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
import { delay } from "lodash";
import * as React from "react";
import {
    createContext,
    FunctionComponent,
    useContext,
    useEffect,
    useState,
} from "react";
import { AppBar } from "../app-bar.component";
import {
    AppBarConfigProviderProps,
    AppBarConfigProviderPropsContextProps,
} from "./app-bar-config-provider.interface";

export const AppBarConfigProvider: FunctionComponent<AppBarConfigProviderProps> =
    ({ children }) => {
        const [showAppNavBar, setShowAppNavBar] = useState(true);
        const [okToRender, setOkToRender] = useState(false);

        useEffect(() => {
            // Slight delay in rendering nav bar helps avoid flicker during initial page redirects
            delay(setOkToRender, 200, true);
        }, []);

        return (
            <AppBarConfigProviderContext.Provider value={{ setShowAppNavBar }}>
                {okToRender && showAppNavBar && <AppBar />}
                {children}
            </AppBarConfigProviderContext.Provider>
        );
    };

const AppBarConfigProviderContext =
    createContext<AppBarConfigProviderPropsContextProps>(
        {} as AppBarConfigProviderPropsContextProps
    );

export const useAppBarConfigProvider =
    (): AppBarConfigProviderPropsContextProps => {
        return useContext(AppBarConfigProviderContext);
    };
