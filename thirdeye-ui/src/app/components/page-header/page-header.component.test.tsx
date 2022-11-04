// Copyright 2022 StarTree Inc

// Licensed under the StarTree Community License (the "License"); you may not use
// this file except in compliance with the License. You may obtain a copy of the
// License at http://www.startree.ai/legal/startree-community-license

// Unless required by applicable law or agreed to in writing, software distributed under the
// License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
// either express or implied.
// See the License for the specific language governing permissions and limitations under
// the License.
import { render, screen } from "@testing-library/react";
import React from "react";
import { BrowserRouter } from "react-router-dom";
import { TimeRangeProvider } from "../time-range/time-range-provider/time-range-provider.component";
import { PageHeader } from "./page-header.component";

jest.mock("../../platform/components", () => ({
    ...(jest.requireActual("../../platform/components") as Record<
        string,
        unknown
    >),
    PageV1: jest.fn().mockImplementation((props) => props.children),
    PageHeaderV1: jest.fn().mockImplementation((props) => props.children),
    PageHeaderTextV1: jest.fn().mockImplementation((props) => props.children),
    PageHeaderActionsV1: jest
        .fn()
        .mockImplementation((props) => props.children),
}));

// Force the time range selector to render fully
jest.mock("@material-ui/core", () => ({
    ...(jest.requireActual("@material-ui/core") as Record<string, unknown>),
    useMediaQuery: jest.fn().mockImplementation(() => true),
}));

jest.mock("i18next", () => ({
    ...(jest.requireActual("i18next") as Record<string, unknown>),
    t: jest.fn().mockImplementation((t) => t),
}));

describe("Page Header", () => {
    it("should render passed title and time range selector", async () => {
        render(
            <BrowserRouter>
                <TimeRangeProvider>
                    <PageHeader showTimeRange title="Hello world" />
                </TimeRangeProvider>
            </BrowserRouter>
        );

        const titleContainer = await screen.getByText("Hello world");

        expect(titleContainer).toBeInTheDocument();

        const timeSelectorContainer = await screen.getByText("label.today");

        expect(timeSelectorContainer).toBeInTheDocument();
    });

    it("should render without time range selector", async () => {
        render(
            <BrowserRouter>
                <TimeRangeProvider>
                    <PageHeader title="Hello world" />
                </TimeRangeProvider>
            </BrowserRouter>
        );

        const titleContainer = await screen.getByText("Hello world");

        expect(titleContainer).toBeInTheDocument();

        const timeSelectorContainer = await screen.queryByText("label.today");

        expect(timeSelectorContainer).toBeNull();

        const createMenuButton = await screen.queryByText("label.create");

        expect(createMenuButton).toBeNull();
    });

    it("should render create menu button if showCreateButton is true", () => {
        render(
            <BrowserRouter>
                <TimeRangeProvider>
                    <PageHeader showCreateButton title="Hello world" />
                </TimeRangeProvider>
            </BrowserRouter>
        );

        const createMenuButton = screen.getByText("label.create");

        expect(createMenuButton).toBeInTheDocument();
    });
});
