// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
import { Paper } from "@material-ui/core";
import React, {
    FunctionComponent,
    HTMLAttributes,
    PropsWithChildren,
} from "react";
import { DimensionV1 } from "../../utils/material-ui/dimension.util";
import { useAutocompletePaperV1Styles } from "./autocomplete-paper-v1.styles";

export const AutocompletePaper: FunctionComponent<
    PropsWithChildren<HTMLAttributes<HTMLElement>>
> = ({ children }: PropsWithChildren<HTMLAttributes<HTMLElement>>) => {
    const autocompletePaperV1Classes = useAutocompletePaperV1Styles();

    return (
        <Paper
            className={autocompletePaperV1Classes.autocompletePaper}
            elevation={DimensionV1.PopoverElevation}
        >
            {children}
        </Paper>
    );
};
