import React, { FunctionComponent } from "react";
import { EmptyStateSwitchProps } from "./empty-state-switch.interfaces";

export const EmptyStateSwitch: FunctionComponent<EmptyStateSwitchProps> = ({
    isEmpty,
    emptyState,
    children,
}) => {
    return (
        <>
            {isEmpty && emptyState}
            {!isEmpty && children}
        </>
    );
};
