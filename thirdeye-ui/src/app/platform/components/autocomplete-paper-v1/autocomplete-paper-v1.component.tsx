/**
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
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
> = ({ children }) => {
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
