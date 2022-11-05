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
import { ThemeProvider } from "@material-ui/core";
import { render, screen } from "@testing-library/react";
import React from "react";
import { lightV1 } from "../../platform/utils";
import { ColorV1 } from "../../platform/utils/material-ui/color.util";
import { ActiveIndicator } from "./active-indicator.component";
import { ActiveIndicatorProps } from "./active-indicator.interfaces";

describe("ActiveIndicator", () => {
    it("should render an indicator for the active state", async () => {
        render(
            <ThemeProvider theme={lightV1}>
                <ActiveIndicator {...mockActiveIndicatorProps} />
            </ThemeProvider>
        );

        expect(
            await screen.findByTestId("activity-indicator-icon")
        ).toHaveAttribute("color", expectedActiveColor);
    });

    it("should render an indicator for the inactive state", async () => {
        const props = { ...mockActiveIndicatorProps, active: false };
        render(
            <ThemeProvider theme={lightV1}>
                <ActiveIndicator {...props} />
            </ThemeProvider>
        );

        expect(
            await screen.findByTestId("activity-indicator-icon")
        ).toHaveAttribute("color", expectedInactiveColor);
    });
});

const mockActiveIndicatorProps = {
    active: true,
} as ActiveIndicatorProps;

const expectedActiveColor: string = ColorV1.Green2;
const expectedInactiveColor: string = ColorV1.Red2;
