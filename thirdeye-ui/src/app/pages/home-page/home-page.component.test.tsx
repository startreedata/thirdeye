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
import { PageContentsGridV1 } from "@startree-ui/platform-ui";
import { act, render, screen } from "@testing-library/react";
import React from "react";
import { HomePage } from "./home-page.component";

jest.mock("@startree-ui/platform-ui", () => ({
    TileButtonIconV1: jest.fn().mockImplementation((props) => props.children),
    TileButtonTextV1: jest.fn().mockImplementation((props) => props.children),
    PageV1: jest.fn().mockImplementation((props) => props.children),
    PageContentsGridV1: jest.fn().mockImplementation((props) => props.children),
    TileButtonV1: jest.fn().mockImplementation((props) => (
        <a data-testid={props.href} href={props.href}>
            {props.children}
        </a>
    )),
}));

jest.mock("../../components/page-header/page-header.component", () => ({
    PageHeader: jest.fn().mockImplementation((props) => props.title),
}));

jest.mock("react-i18next", () => ({
    useTranslation: jest.fn().mockReturnValue({
        t: (key: string) => key,
    }),
}));

jest.mock("../../utils/routes/routes.util", () => ({
    getAlertsPath: jest.fn().mockReturnValue("testAlertsPath"),
    getAnomaliesPath: jest.fn().mockReturnValue("testAnomaliesPath"),
    getConfigurationPath: jest.fn().mockReturnValue("testConfigurationPath"),
    getSubscriptionGroupsPath: jest
        .fn()
        .mockReturnValue("testSubscriptionGroupsPath"),
    getDatasetsPath: jest.fn().mockReturnValue("testDatasetsPath"),
    getDatasourcesPath: jest.fn().mockReturnValue("testDatasourcesPath"),
    getMetricsPath: jest.fn().mockReturnValue("testMetricsPath"),
}));

describe("Home Page", () => {
    it("should set appropriate page title", async () => {
        act(() => {
            render(<HomePage />);
        });

        expect(PageContentsGridV1).toHaveBeenCalledWith(
            {
                children: expect.any(Object),
            },
            {}
        );
    });

    it("should render all navigation buttons", async () => {
        act(() => {
            render(<HomePage />);
        });

        expect(screen.getByText("label.alerts")).toBeInTheDocument();
        expect(screen.getByText("label.anomalies")).toBeInTheDocument();
        expect(screen.getByText("label.configuration")).toBeInTheDocument();
    });

    it("should have proper link to alerts path on alerts icon button", async () => {
        act(() => {
            render(<HomePage />);
        });

        expect(screen.getByTestId(TEST_PATHS.alerts)).toHaveAttribute(
            "href",
            TEST_PATHS.alerts
        );
    });

    it("should have proper link to anomalies path on anomalies icon button", async () => {
        act(() => {
            render(<HomePage />);
        });

        expect(screen.getByTestId(TEST_PATHS.anomalies)).toHaveAttribute(
            "href",
            TEST_PATHS.anomalies
        );
    });

    it("should have proper link to configuration path on configuration icon button", async () => {
        act(() => {
            render(<HomePage />);
        });

        expect(screen.getByTestId(TEST_PATHS.configuration)).toHaveAttribute(
            "href",
            TEST_PATHS.configuration
        );
    });
});

const TEST_PATHS = {
    alerts: "testAlertsPath",
    anomalies: "testAnomaliesPath",
    configuration: "testConfigurationPath",
    subscriptionGroups: "testSubscriptionGroupsPath",
    datasets: "testDatasetsPath",
    datasources: "testDatasourcesPath",
    metrics: "testMetricsPath",
};
