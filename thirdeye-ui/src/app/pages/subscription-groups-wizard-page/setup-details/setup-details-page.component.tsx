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
import { default as React, FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate, useOutletContext } from "react-router-dom";
import { SubscriptionGroupDetails } from "../../../components/subscription-group-wizard/subscription-group-details/subscription-group-details.component";
import { WizardBottomBar } from "../../../components/welcome-onboard-datasource/wizard-bottom-bar/wizard-bottom-bar.component";
import { PageContentsGridV1 } from "../../../platform/components";
import { AppRouteRelative } from "../../../utils/routes/routes.util";
import { SubscriptionGroupsWizardPageOutletContext } from "../subscription-groups-wizard-page.interfaces";

export const SetupDetailsPage: FunctionComponent = () => {
    const navigate = useNavigate();
    const { t } = useTranslation();

    const handleOnNextClick = (): void => {
        navigate(
            `../${AppRouteRelative.SUBSCRIPTION_GROUPS_WIZARD_ALERT_DIMENSIONS}`
        );
    };

    const { subscriptionGroup, setSubscriptionGroup, onCancel } =
        useOutletContext<SubscriptionGroupsWizardPageOutletContext>();

    return (
        <>
            <PageContentsGridV1>
                <SubscriptionGroupDetails
                    subscriptionGroup={subscriptionGroup}
                    onChange={setSubscriptionGroup}
                />
            </PageContentsGridV1>
            <WizardBottomBar
                backButtonLabel={t("label.cancel")}
                handleBackClick={onCancel}
                handleNextClick={handleOnNextClick}
                nextButtonLabel={t("label.next")}
            />
        </>
    );
};
