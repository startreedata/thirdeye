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
import { Box, Card, CardContent, Grid } from "@material-ui/core";
import { isEmpty } from "lodash";
import { default as React, FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate, useOutletContext } from "react-router-dom";
import { NoDataIndicator } from "../../../components/no-data-indicator/no-data-indicator.component";
import { EmptyStateSwitch } from "../../../components/page-states/empty-state-switch/empty-state-switch.component";
import { AlertsDimensions } from "../../../components/subscription-group-wizard/alerts-dimensions/alerts-dimensions.component";
import {
    cleanUpAssociations,
    validateSubscriptionGroup,
} from "../../../components/subscription-group-wizard/subscription-group-wizard.utils";
import { WizardBottomBar } from "../../../components/welcome-onboard-datasource/wizard-bottom-bar/wizard-bottom-bar.component";
import {
    PageContentsGridV1,
    useDialogProviderV1,
} from "../../../platform/components";
import { DialogType } from "../../../platform/components/dialog-provider-v1/dialog-provider-v1.interfaces";
import { SubscriptionGroup } from "../../../rest/dto/subscription-group.interfaces";
import { AppRouteRelative } from "../../../utils/routes/routes.util";
import { SubscriptionGroupsWizardPageOutletContext } from "../subscription-groups-wizard-page.interfaces";

export const SetupAlertDimensionsPage: FunctionComponent = () => {
    const navigate = useNavigate();
    const { t } = useTranslation();
    const { showDialog } = useDialogProviderV1();

    const {
        subscriptionGroup,
        alerts,
        enumerationItems,
        associations,
        submitButtonLabel,
        setAssociations,
        onFinish,
    } = useOutletContext<SubscriptionGroupsWizardPageOutletContext>();

    const submitButtonDisabled = !validateSubscriptionGroup(subscriptionGroup);

    const handleBackClick = (): void => {
        navigate(`../${AppRouteRelative.SUBSCRIPTION_GROUPS_WIZARD_DETAILS}`);
    };

    const handleNextClick = (): void => {
        if (isEmpty(subscriptionGroup.alertAssociations)) {
            // If there are no alert associations, show a dialog confirming if this is intended
            handleSubscriptionGroupWizardFinishDialog(subscriptionGroup);
        } else {
            if (subscriptionGroup.alertAssociations) {
                subscriptionGroup.alertAssociations = cleanUpAssociations(
                    subscriptionGroup.alertAssociations
                );
            }
            // Otherwise, proceed with saving the data
            onFinish(subscriptionGroup);
        }
    };

    const handleSubscriptionGroupWizardFinishDialog = (
        subscriptionGroup: SubscriptionGroup
    ): void => {
        showDialog({
            type: DialogType.ALERT,
            contents: t(
                "message.are-you-sure-to-proceed-without-any-alerts-dimensions-added-to-subscription-group"
            ),
            okButtonText: t("label.yes"),
            cancelButtonText: t("label.no"),
            onOk: () => onFinish(subscriptionGroup),
            keepDialogOnOk: true,
        });
    };

    return (
        <>
            <PageContentsGridV1>
                <Grid item xs={12}>
                    <EmptyStateSwitch
                        emptyState={
                            <Card>
                                <CardContent>
                                    <Box padding={20} textAlign="center">
                                        <NoDataIndicator>
                                            <Box textAlign="center">
                                                {t(
                                                    "message.no-entity-created",
                                                    {
                                                        entity: t(
                                                            "label.alerts"
                                                        ),
                                                    }
                                                )}
                                            </Box>
                                        </NoDataIndicator>
                                    </Box>
                                </CardContent>
                            </Card>
                        }
                        isEmpty={isEmpty(alerts)}
                    >
                        <AlertsDimensions
                            alerts={alerts}
                            associations={associations}
                            enumerationItems={enumerationItems}
                            setAssociations={setAssociations}
                        />
                    </EmptyStateSwitch>
                </Grid>
            </PageContentsGridV1>
            <WizardBottomBar
                backButtonLabel={t("label.back")}
                handleBackClick={handleBackClick}
                handleNextClick={handleNextClick}
                nextButtonIsDisabled={submitButtonDisabled}
                nextButtonLabel={submitButtonLabel}
            />
        </>
    );
};
