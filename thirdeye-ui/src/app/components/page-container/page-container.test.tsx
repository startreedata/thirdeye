import { act, cleanup, render, screen } from "@testing-library/react";
import React, { ReactNode } from "react";
import { PageContainer } from "./page-container.component";
import { PageContainerProps } from "./page-container.interfaces";

describe("PageContainer", () => {
    beforeEach(() => cleanup);

    it("component should load children correctly", async () => {
        act(() => {
            render(<PageContainer {...mockDefaultProps} />);
        });

        expect(await screen.findByText("TestChildren")).toBeInTheDocument();
    });

    it("component should not load children if they aren't passed", async () => {
        const props = { ...mockDefaultProps, children: null };
        act(() => {
            render(<PageContainer {...props} />);
        });

        expect(screen.queryByText("TestChildren")).not.toBeInTheDocument();
    });
});

const mockDefaultProps = {
    children: (<p>TestChildren</p>) as ReactNode,
} as PageContainerProps;
