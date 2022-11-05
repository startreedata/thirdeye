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
import React from "react";
import { NoDataIndicator } from "./no-data-indicator.component";
import { NoDataIndicatorProps } from "./no-data-indicator.interfaces";

describe("NoDataIndicator", () => {
    it("should load text prop correctly", async () => {
        render(<NoDataIndicator {...mockDefaultProps} />);

        expect(await screen.findByText("TestText")).toBeInTheDocument();
    });

    it("should not load text if it's not passed", async () => {
        const props = { ...mockDefaultProps, text: "" };
        render(<NoDataIndicator {...props} />);

        expect(screen.queryByText("TestText")).not.toBeInTheDocument();
    });
});

const mockDefaultProps = {
    text: "TestText",
} as NoDataIndicatorProps;
