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
import { render, screen } from "@testing-library/react";
import React, { ReactNode } from "react";
import { TooltipWithBounds } from "./tooltip-with-bounds.component";
import { TooltipWithBoundsProps } from "./tooltip-with-bounds.interfaces";

describe("TooltipWithBounds", () => {
    it("should load title and children correctly", async () => {
        render(
            <TooltipWithBounds {...mockDefaultProps}>
                <p>TestChildren</p>
            </TooltipWithBounds>
        );

        expect(await screen.findByText("TestTitle")).toBeInTheDocument();
        expect(await screen.findByText("TestChildren")).toBeInTheDocument();
    });

    it("should not load title if tooltip is not open", async () => {
        const props = { ...mockDefaultProps, open: false };
        render(
            <TooltipWithBounds {...props}>
                <p>TestChildren</p>
            </TooltipWithBounds>
        );

        expect(screen.queryByText("TestTitle")).not.toBeInTheDocument();
    });
});

const mockDefaultProps = {
    top: 10,
    left: 10,
    open: true,
    title: "TestTitle" as ReactNode,
} as TooltipWithBoundsProps;
