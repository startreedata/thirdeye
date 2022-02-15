// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
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
