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
import { PageContentsGridV1 } from "../../platform/components";
import { PageNotFoundPage } from "./page-not-found-page.component";

jest.mock("react-i18next", () => ({
    useTranslation: jest.fn().mockReturnValue({
        t: (key: string) => key,
    }),
}));

jest.mock("../../platform/components", () => ({
    ...(jest.requireActual("../../platform/components") as Record<
        string,
        unknown
    >),
    PageContentsGridV1: jest.fn().mockImplementation((props) => props.children),
    PageHeaderTextV1: jest.fn().mockImplementation((props) => props.children),
    PageHeaderV1: jest.fn().mockImplementation((props) => props.children),
    PageNotFoundIndicatorV1: jest.fn().mockReturnValue("page-not-found"),
    PageV1: jest.fn().mockImplementation((props) => props.children),
}));

jest.mock("../../components/page-header/page-header.component", () => ({
    PageHeader: jest.fn().mockImplementation((props) => props.children),
}));

describe("Page Not Found Page", () => {
    it("should set appropriate page title", async () => {
        act(() => {
            render(<PageNotFoundPage />);
        });

        expect(PageContentsGridV1).toHaveBeenCalledWith(
            {
                fullHeight: true,
                children: expect.any(Object),
            },
            {}
        );
    });

    it("should render page not found indicator", async () => {
        act(() => {
            render(<PageNotFoundPage />);
        });

        expect(screen.getByText("page-not-found")).toBeInTheDocument();
    });
});
