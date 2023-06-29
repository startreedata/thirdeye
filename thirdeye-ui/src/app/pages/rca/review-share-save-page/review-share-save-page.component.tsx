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
import {
    Box,
    Button,
    Card,
    CardContent,
    CardHeader,
    Grid,
} from "@material-ui/core";
import React, { FunctionComponent, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { useOutletContext } from "react-router-dom";
import { InvestigationDetailsForm } from "../../../components/rca/investigation-details-form/investigation-details-form.component";
import { InvestigationPreview } from "../../../components/rca/investigation-preview/investigation-preview.component";
import { useNotificationProviderV1 } from "../../../platform/components";
import { ActionStatus } from "../../../rest/actions.interfaces";
import {
    createInvestigation,
    updateInvestigation,
} from "../../../rest/rca/rca.rest";
import { notifyIfErrors } from "../../../utils/notifications/notifications.util";
import { InvestigationContext } from "../investigation-state-tracker-container-page/investigation-state-tracker.interfaces";

export const ReviewShareSavePage: FunctionComponent = () => {
    const { t } = useTranslation();
    const { notify } = useNotificationProviderV1();
    const {
        alertInsight,
        anomaly,
        investigation,
        handleServerUpdatedInvestigation,
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
            return "message.create-error";
        }

        return "message.update-error";
    }, [investigation]);

    const handleSaveClick = (): void => {
        investigation.name = investigationName;
        investigation.text = investigationText;
        setIsSaving(true);

        serverRequestRestFunction(investigation)
            .then((investigationFromServer) => {
                handleServerUpdatedInvestigation(investigationFromServer);
            })
            .catch((response) => {
                notifyIfErrors(
                    ActionStatus.Error,
                    response?.response?.data?.list,
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
                                <Box pt={2} textAlign="right">
                                    <Button
                                        color="primary"
                                        disabled={isSaving}
                                        variant="outlined"
                                        onClick={handleSaveClick}
                                    >
                                        {investigation.id === undefined && (
                                            <>
                                                {!isSaving &&
                                                    t(
                                                        "label.save-investigation"
                                                    )}
                                            </>
                                        )}
                                        {investigation.id !== undefined && (
                                            <>
                                                {!isSaving &&
                                                    t("label.save-progress")}
                                            </>
                                        )}
                                    </Button>
                                </Box>
                            </CardContent>
                        </Card>
                    </Box>
                </InvestigationPreview>
            </Grid>
        </>
    );
};
