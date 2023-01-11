/*
 * Copyright 2022 StarTree Inc
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
import { Button } from "@material-ui/core";
import CommentIcon from "@material-ui/icons/Comment";
import { isEmpty, isEqual } from "lodash";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import {
    NotificationTypeV1,
    useNotificationProviderV1,
} from "../../../platform/components";
import {
    RCA_INVESTIGATE_TOUR_IDS,
    useAppTour,
} from "../../../platform/components/app-walkthrough-v1/app-walkthrough-v1.utils";
import { Investigation } from "../../../rest/dto/rca.interfaces";
import {
    createInvestigation,
    updateInvestigation,
} from "../../../rest/rca/rca.rest";
import { InvestigationOptionsProps } from "./investigation-options.interfaces";
import { ModifyInvestigationDialog } from "./modify-investigation-dialog/modify-investigation-dialog.component";

export const InvestigationOptions: FunctionComponent<InvestigationOptionsProps> =
    ({
        localInvestigation,
        serverInvestigation,
        investigationId,
        onSuccessfulUpdate,
        onRemoveInvestigationAssociation,
    }) => {
        const { t } = useTranslation();
        const { notify } = useNotificationProviderV1();
        const [
            isLocalInvestigationInSync,
            setIsLocalInvestigationDifferentInSync,
        ] = useState(false);
        const [shouldShowCreateDialog, setShouldShowCreateDialog] =
            useState(false);
        const [shouldShowUpdateDialog, setShouldShowUpdateDialog] =
            useState(false);
        const [isSaving, setIsSaving] = useState(false);

        useEffect(() => {
            if (serverInvestigation && serverInvestigation.uiMetadata) {
                setIsLocalInvestigationDifferentInSync(
                    isEqual(
                        localInvestigation.uiMetadata,
                        serverInvestigation.uiMetadata
                    )
                );
            }
        }, [localInvestigation, serverInvestigation]);

        const handleSuccessfulSave = (
            newInvestigation: Investigation
        ): void => {
            notify(
                NotificationTypeV1.Success,
                t("message.create-success", {
                    entity: t("label.investigation"),
                })
            );
            setShouldShowCreateDialog(false);

            onSuccessfulUpdate(newInvestigation);
        };

        const handleSuccessfulUpdate = (
            updatedInvestigation: Investigation
        ): void => {
            notify(
                NotificationTypeV1.Success,
                t("message.update-success", {
                    entity: t("label.investigation"),
                })
            );

            setShouldShowUpdateDialog(false);

            onSuccessfulUpdate(updatedInvestigation);
        };

        const handleUpdateClick = (): void => {
            setIsSaving(true);
            updateInvestigation(localInvestigation)
                .then((updatedInvestigationFromServer) => {
                    notify(
                        NotificationTypeV1.Success,
                        t("message.update-success", {
                            entity: t("label.investigation"),
                        })
                    );
                    onSuccessfulUpdate(updatedInvestigationFromServer);
                })
                .catch((response) => {
                    // Default to a generic error message
                    let errorMessages: string[] = [
                        t("message.update-error", {
                            entity: t("label.investigation"),
                        }),
                    ];

                    // If messages exist from server, use those instead
                    if (
                        response &&
                        response.response &&
                        response.response.data
                    ) {
                        if (!isEmpty(response.response.data.list)) {
                            errorMessages = response.response.data.list.map(
                                (error: { msg: string }) => error.msg
                            );
                        }
                    }
                    errorMessages.forEach((msg) =>
                        notify(NotificationTypeV1.Error, msg)
                    );
                })
                .finally(() => setIsSaving(false));
        };

        const { startTour } = useAppTour("RCA_INVESTIGATE");

        const handleStartTour = (): void => {
            startTour();
        };

        return (
            <>
                {investigationId === null && (
                    <Button
                        color="primary"
                        data-tour-id={
                            RCA_INVESTIGATE_TOUR_IDS.SAVE_INVESTIGATION
                        }
                        variant="contained"
                        onClick={() => setShouldShowCreateDialog(true)}
                    >
                        {t("label.save-investigation")}
                    </Button>
                )}
                {shouldShowCreateDialog && (
                    <ModifyInvestigationDialog
                        actionLabelIdentifier="label.create-entity"
                        errorGenericMsgIdentifier="message.create-error"
                        investigation={localInvestigation}
                        serverRequestRestFunction={createInvestigation}
                        onClose={() => setShouldShowCreateDialog(false)}
                        onSuccessfulSave={handleSuccessfulSave}
                    />
                )}

                {investigationId !== null && serverInvestigation !== null && (
                    <>
                        <Button
                            color="primary"
                            variant="contained"
                            onClick={() => setShouldShowUpdateDialog(true)}
                        >
                            <CommentIcon />
                        </Button>
                        <Button
                            color="primary"
                            disabled={isLocalInvestigationInSync || isSaving}
                            variant="contained"
                            onClick={handleUpdateClick}
                        >
                            {isSaving && t("label.saving")}
                            {!isSaving && t("label.save-progress")}
                        </Button>
                    </>
                )}
                <Button variant="contained" onClick={handleStartTour}>
                    {t("label.help")}
                </Button>

                {shouldShowUpdateDialog && (
                    <ModifyInvestigationDialog
                        actionLabelIdentifier="label.update-entity"
                        errorGenericMsgIdentifier="message.update-error"
                        investigation={localInvestigation}
                        serverRequestRestFunction={updateInvestigation}
                        onClose={() => setShouldShowUpdateDialog(false)}
                        onSuccessfulSave={handleSuccessfulUpdate}
                    />
                )}

                {/* Request for investigation experienced error */}
                {investigationId !== null && serverInvestigation === null && (
                    <Button
                        color="primary"
                        variant="contained"
                        onClick={onRemoveInvestigationAssociation}
                    >
                        Remove Investigation Association
                    </Button>
                )}
            </>
        );
    };
