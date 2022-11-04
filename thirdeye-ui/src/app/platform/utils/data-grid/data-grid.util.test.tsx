// Copyright 2022 StarTree Inc

// Licensed under the StarTree Community License (the "License"); you may not use
// this file except in compliance with the License. You may obtain a copy of the
// License at http://www.startree.ai/legal/startree-community-license

// Unless required by applicable law or agreed to in writing, software distributed under the
// License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
// either express or implied.
// See the License for the specific language governing permissions and limitations under
// the License.
import { act, render, screen } from "@testing-library/react";
import React from "react";
import { BrowserRouter } from "react-router-dom";
import { LinkV1 } from "../../components/link-v1/link-v1.component";
import { TooltipV1 } from "../../components/tooltip-v1/tooltip-v1.component";
import { linkRendererV1 } from "./data-grid.util";

jest.mock("../../components/tooltip-v1/tooltip-v1.component", () => ({
    TooltipV1: jest.fn().mockImplementation((props) => props.children),
}));

jest.mock("../../components/link-v1/link-v1.component", () => ({
    LinkV1: jest.fn().mockImplementation((props) => props.children),
}));

describe("Data Grid Util", () => {
    it("linkRendererV1 should render link with default inputs", () => {
        act(() => {
            render(
                <BrowserRouter>
                    {linkRendererV1("testContents", "testHref")}
                </BrowserRouter>
            );
        });

        expect(TooltipV1).toHaveBeenCalledWith(
            {
                children: expect.any(Object),
                title: "testContents",
            },
            {}
        );
        expect(LinkV1).toHaveBeenCalledWith(
            {
                href: "testHref",
                variant: "body2",
                children: "testContents",
            },
            {}
        );
        expect(screen.getByText("testContents")).toBeInTheDocument();
    });

    it("linkRendererV1 should render link with default tooltip", () => {
        act(() => {
            render(
                <BrowserRouter>
                    {linkRendererV1(
                        "testContents",
                        "testHref",
                        false,
                        true,
                        false,
                        "_blank",
                        mockOnClick
                    )}
                </BrowserRouter>
            );
        });

        expect(TooltipV1).toHaveBeenCalledWith(
            {
                children: expect.any(Object),
                title: "testContents",
            },
            {}
        );
        expect(LinkV1).toHaveBeenCalledWith(
            {
                href: "testHref",
                externalLink: false,
                target: "_blank",
                disabled: false,
                variant: "body2",
                onClick: mockOnClick,
                children: "testContents",
            },
            {}
        );
        expect(screen.getByText("testContents")).toBeInTheDocument();
    });

    it("linkRendererV1 should render link with custom tooltip", () => {
        act(() => {
            render(
                <BrowserRouter>
                    {linkRendererV1(
                        "testContents",
                        "testHref",
                        false,
                        "testTooltip",
                        false,
                        "_blank",
                        mockOnClick
                    )}
                </BrowserRouter>
            );
        });

        expect(TooltipV1).toHaveBeenCalledWith(
            {
                children: expect.any(Object),
                title: "testTooltip",
            },
            {}
        );
        expect(LinkV1).toHaveBeenCalledWith(
            {
                href: "testHref",
                externalLink: false,
                target: "_blank",
                disabled: false,
                variant: "body2",
                onClick: mockOnClick,
                children: "testContents",
            },
            {}
        );
        expect(screen.getByText("testContents")).toBeInTheDocument();
    });

    it("linkRendererV1 should render link without tooltip", () => {
        act(() => {
            render(
                <BrowserRouter>
                    {linkRendererV1(
                        "testContents",
                        "testHref",
                        false,
                        false,
                        false,
                        "_blank",
                        mockOnClick
                    )}
                </BrowserRouter>
            );
        });

        expect(TooltipV1).not.toHaveBeenCalled();
        expect(LinkV1).toHaveBeenCalledWith(
            {
                href: "testHref",
                externalLink: false,
                target: "_blank",
                disabled: false,
                variant: "body2",
                onClick: mockOnClick,
                children: "testContents",
            },
            {}
        );
        expect(screen.getByText("testContents")).toBeInTheDocument();
    });
});

const mockOnClick = jest.fn();
