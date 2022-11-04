// Copyright 2022 StarTree Inc

// Licensed under the StarTree Community License (the "License"); you may not use
// this file except in compliance with the License. You may obtain a copy of the
// License at http://www.startree.ai/legal/startree-community-license

// Unless required by applicable law or agreed to in writing, software distributed under the
// License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
// either express or implied.
// See the License for the specific language governing permissions and limitations under
// the License.
import { useTheme } from "@material-ui/core";
import FiberManualRecordIcon from "@material-ui/icons/FiberManualRecord";
import React, { FunctionComponent } from "react";
import { ActiveIndicatorProps } from "./active-indicator.interfaces";
import { useActiveIndicatorStyles } from "./active-indicator.styles";

export const ActiveIndicator: FunctionComponent<ActiveIndicatorProps> = (
    props: ActiveIndicatorProps
) => {
    const activeIndicatorClasses = useActiveIndicatorStyles();
    const theme = useTheme();

    const getIndicatorColor = (): string => {
        if (props.active) {
            return theme.palette.success.main;
        }

        return theme.palette.error.main;
    };

    return (
        <span className={activeIndicatorClasses.indicator}>
            <FiberManualRecordIcon
                data-testid="activity-indicator-icon"
                fontSize="small"
                htmlColor={getIndicatorColor()}
            />
        </span>
    );
};
