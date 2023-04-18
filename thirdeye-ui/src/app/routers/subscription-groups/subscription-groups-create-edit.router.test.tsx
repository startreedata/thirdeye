/*
 * Copyright 2023 StarTree Inc
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
import { render, screen } from "@testing-library/react";
import React, { FunctionComponent } from "react";
import { MemoryRouter, Outlet } from "react-router-dom";
import { AppRouteRelative } from "../../utils/routes/routes.util";
import { SubscriptionGroupsCreateEditRouter } from "./subscription-groups-create-edit.router";

jest.mock(
    "../../platform/components/app-loading-indicator-v1/app-loading-indicator-v1.component",
    () => ({
        AppLoadingIndicatorV1: jest
            .fn()
            .mockReturnValue("testLoadingIndicatorV1"),
    })
);

jest.mock(
    "../../pages/subscription-groups-wizard-page/setup-details/setup-details-page.component",
    () => ({
        SetupDetailsPage: jest
            .fn()
            .mockReturnValue("testSubscriptionGroupsWizardDetailsPage"),
    })
);

jest.mock(
    "../../pages/subscription-groups-wizard-page/setup-alert-dimensions/setup-alert-dimensions-page.component",
    () => ({
        SetupAlertDimensionsPage: jest
            .fn()
            .mockReturnValue("testSubscriptionGroupsWizardAlertDimensionsPage"),
    })
);

jest.mock(
    "../../pages/page-not-found-page/page-not-found-page.component",
    () => ({
        PageNotFoundPage: jest.fn().mockReturnValue("testPageNotFoundPage"),
    })
);

const MockContainer: FunctionComponent = () => (
    <div>
        <Outlet />
    </div>
);

describe("Subscription Groups Create Edit Router", () => {
    it("should redirect to details route by default", async () => {
        render(
            <MemoryRouter initialEntries={["/"]}>
                <SubscriptionGroupsCreateEditRouter
                    containerPage={<MockContainer />}
                />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testSubscriptionGroupsWizardDetailsPage")
        ).resolves.toBeInTheDocument();
    });

    it("should should load subscription group details page for route", async () => {
        render(
            <MemoryRouter
                initialEntries={[
                    `/${AppRouteRelative.SUBSCRIPTION_GROUPS_WIZARD_DETAILS}`,
                ]}
            >
                <SubscriptionGroupsCreateEditRouter
                    containerPage={<MockContainer />}
                />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testSubscriptionGroupsWizardDetailsPage")
        ).resolves.toBeInTheDocument();
    });

    it("should should load alert dimension page for route", async () => {
        render(
            <MemoryRouter
                initialEntries={[
                    `/${AppRouteRelative.SUBSCRIPTION_GROUPS_WIZARD_ALERT_DIMENSIONS}`,
                ]}
            >
                <SubscriptionGroupsCreateEditRouter
                    containerPage={<MockContainer />}
                />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testSubscriptionGroupsWizardAlertDimensionsPage")
        ).resolves.toBeInTheDocument();
    });

    it("should render page not found page at invalid subscription groups path", async () => {
        render(
            <MemoryRouter initialEntries={[`/testPageNotFound`]}>
                <SubscriptionGroupsCreateEditRouter
                    containerPage={<MockContainer />}
                />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testPageNotFoundPage")
        ).resolves.toBeInTheDocument();
    });
});
