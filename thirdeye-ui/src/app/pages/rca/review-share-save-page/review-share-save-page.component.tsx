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
import { Box, Card, CardContent, CardHeader, Grid } from "@material-ui/core";
import React, { FunctionComponent, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { useOutletContext, useSearchParams } from "react-router-dom";
import { InvestigationDetailsForm } from "../../../components/rca/investigation-details-form/investigation-details-form.component";
import { InvestigationPreview } from "../../../components/rca/investigation-preview/investigation-preview.component";
import { WizardBottomBar } from "../../../components/welcome-onboard-datasource/wizard-bottom-bar/wizard-bottom-bar.component";
import {
    NotificationTypeV1,
    useNotificationProviderV1,
} from "../../../platform/components";
import { ActionStatus } from "../../../rest/actions.interfaces";
import {
    createInvestigation,
    updateInvestigation,
} from "../../../rest/rca/rca.rest";
import { notifyIfErrors } from "../../../utils/notifications/notifications.util";
import { AppRouteRelative } from "../../../utils/routes/routes.util";
import { InvestigationContext } from "../investigation-state-tracker-container-page/investigation-state-tracker.interfaces";

export const ReviewShareSavePage: FunctionComponent = () => {
    const { t } = useTranslation();
    const [searchParams] = useSearchParams();
    const { notify } = useNotificationProviderV1();
    const {
        alertInsight,
        anomaly,
        investigation,
        handleServerUpdatedInvestigation,
        onInvestigationChange,
    } = useOutletContext<InvestigationContext>();

    const [isSaving, setIsSaving] = useState(false);
    const [investigationName, setInvestigationName] = useState(
        investigation.name
    );
    const [investigationText, setInvestigationText] = useState(
        investigation.text
    );
    const serverRequestRestFunction = useMemo(() => {
        if (investigation.id) {
            return updateInvestigation;
        }

        return createInvestigation;
    }, [investigation]);
    const errorGenericMsgIdentifier = useMemo(() => {
        if (investigation.id) {
            return "message.update-error";
        }

        return "message.create-error";
    }, [investigation]);

    const saveButtonLabel = useMemo(() => {
        if (!isSaving) {
            if (investigation.id === undefined) {
                return t("label.save-investigation");
            } else {
                return t("label.save-progress");
            }
        }

        return t("label.saving");
    }, [isSaving, investigation]);

    const handleSaveClick = (): void => {
        if (!investigationName) {
            notify(
                NotificationTypeV1.Error,
                "Please enter a name for the investigation in the investigation details section at the bottom of the page"
            );

            return;
        }

        investigation.name = investigationName;
        investigation.text = investigationText;

        setIsSaving(true);

        serverRequestRestFunction(investigation)
            .then((investigationFromServer) => {
                handleServerUpdatedInvestigation(investigationFromServer);

                if (investigation.id) {
                    notify(
                        NotificationTypeV1.Success,
                        "Successfully updated investigation"
                    );
                } else {
                    notify(
                        NotificationTypeV1.Success,
                        "Successfully created investigation, copy and paste URL to share the investigation"
                    );
                }
            })
            .catch((response) => {
                notifyIfErrors(
                    ActionStatus.Error,
                    response?.data?.list,
                    notify,
                    t(errorGenericMsgIdentifier, {
                        entity: t("label.investigation"),
                    })
                );
            })
            .finally(() => {
                setIsSaving(false);
            });
    };

    return (
        <>
            <Grid item xs={12}>
                <InvestigationPreview
                    alertInsight={alertInsight}
                    anomaly={anomaly}
                    investigation={investigation}
                    title={t("label.review-investigation-share")}
                    onInvestigationChange={onInvestigationChange}
                >
                    <Box pt={3}>
                        <Card>
                            <CardHeader
                                title={t("label.investigation-details")}
                            />
                            <CardContent>
                                <InvestigationDetailsForm
                                    investigation={investigation}
                                    onCommentChange={setInvestigationText}
                                    onNameChange={setInvestigationName}
                                />
                            </CardContent>
                        </Card>
                    </Box>
                </InvestigationPreview>
            </Grid>

            <WizardBottomBar
                backBtnLink={`../${
                    AppRouteRelative.RCA_EVENTS
                }?${searchParams.toString()}`}
                handleNextClick={handleSaveClick}
                nextButtonIsDisabled={isSaving}
                nextButtonLabel={saveButtonLabel}
            />
        </>
    );
};
