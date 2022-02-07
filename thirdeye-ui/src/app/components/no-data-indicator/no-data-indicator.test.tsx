import { render, screen } from "@testing-library/react";
import React from "react";
import { NoDataIndicator } from "./no-data-indicator.component";
import { NoDataIndicatorProps } from "./no-data-indicator.interfaces";

describe("NoDataIndicator", () => {
    it("component should load text prop correctly", async () => {
        render(<NoDataIndicator {...mockDefaultProps} />);

        expect(await screen.findByText("TestText")).toBeInTheDocument();
    });

    it("component should not load text if it's not passed", async () => {
        const props = { ...mockDefaultProps, text: "" };
        render(<NoDataIndicator {...props} />);

        expect(screen.queryByText("TestText")).not.toBeInTheDocument();
    });
});

const mockDefaultProps = {
    text: "TestText",
} as NoDataIndicatorProps;
