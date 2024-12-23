/*
 * Copyright 2024 StarTree Inc
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
import { test } from "@playwright/test";
import { OnboardingPage } from "../pages/onboarding";

test("Onboarding page", async ({ page }) => {
    const onboardingPage = new OnboardingPage(page);
    await page.route("*/**/api/alerts/count", async (route) => {
        const json = {
            count: 0,
        };
        await route.fulfill({ json });
    });
    await onboardingPage.goToWelcomeLanding();
    await onboardingPage.resolveApis();
    await onboardingPage.checkHeader();
    await onboardingPage.clickCreateAlertButton();
    await onboardingPage.resolveCreateAlertApis();
    await onboardingPage.checkCreateAlertHeader();
});
