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
import classNames from "classnames";
import React, { createContext, FunctionComponent, useContext } from "react";
import { HelmetProvider } from "react-helmet-async";
import {
    AppContainerV1ContextProps,
    AppContainerV1Props,
} from "./app-container-v1.interfaces";
import { useAppContainerV1Styles } from "./app-container-v1.styles";

export const AppContainerV1: FunctionComponent<AppContainerV1Props> = ({
    name,
    className,
    children,
    ...otherProps
}) => {
    const appContainerV1Classes = useAppContainerV1Styles();

    const appContainerV1Context = {
        name: name,
    };

    return (
        <HelmetProvider>
            <AppContainerV1Context.Provider value={appContainerV1Context}>
                <div
                    {...otherProps}
                    className={classNames(
                        appContainerV1Classes.appContainer,
                        className,
                        "app-container-v1"
                    )}
                >
                    {children}
                </div>
            </AppContainerV1Context.Provider>
        </HelmetProvider>
    );
};

const AppContainerV1Context = createContext<AppContainerV1ContextProps>(
    {} as AppContainerV1ContextProps
);

export const useAppContainerV1 = (): AppContainerV1ContextProps => {
    return useContext(AppContainerV1Context);
};
