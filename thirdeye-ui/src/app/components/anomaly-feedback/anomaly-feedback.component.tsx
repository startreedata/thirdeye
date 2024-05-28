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
import { Box, Button, ButtonGroup, Grid, TextField } from "@material-ui/core";
import CommentIcon from "@material-ui/icons/Comment";
import KeyboardArrowDownIcon from "@material-ui/icons/KeyboardArrowDown";
import { AxiosError } from "axios";
import { isEmpty } from "lodash";
import React, {
    FunctionComponent,
    MouseEvent,
    useEffect,
    useRef,
    useState,
} from "react";
import { useTranslation } from "react-i18next";
import {
    DropdownMenuV1,
    NotificationTypeV1,
    TooltipV1,
    useDialogProviderV1,
    useNotificationProviderV1,
} from "../../platform/components";
import { DialogType } from "../../platform/components/dialog-provider-v1/dialog-provider-v1.interfaces";
import { updateAnomalyFeedback } from "../../rest/anomalies/anomalies.rest";
import { AnomalyFeedbackType } from "../../rest/dto/anomaly.interfaces";
import {
    ALL_OPTIONS_TO_DESCRIPTIONS,
    ALL_OPTIONS_WITH_NO_FEEDBACK,
} from "../../utils/anomalies/anomalies.util";
import { getErrorMessages } from "../../utils/rest/rest.util";
import { AnomalyFeedbackProps } from "./anomaly-feedback.interfaces";

export const AnomalyFeedback: FunctionComponent<AnomalyFeedbackProps> = ({
    anomalyId,
    anomalyFeedback,
    hideComment,
}) => {
    const [currentlySelected, setCurrentlySelected] =
        useState<AnomalyFeedbackType>(anomalyFeedback.type);
    const [modifiedFeedbackComment, setModifiedFeedbackComment] = useState(
        anomalyFeedback.comment
    );
    const [updateHasError, setUpdateHasError] = useState(false);
    const { showDialog } = useDialogProviderV1();
    const { notify } = useNotificationProviderV1();
    const { t } = useTranslation();
    /*
     * use a ref because there's a strange bug when the TextField in a dialog
     * does not update the value through onChange callback
     */
    const commentRef = useRef<HTMLInputElement>();

    // Feedback menu
    const [feedbackMenuAnchorEl, setFeedbackMenuAnchorEl] =
        React.useState<HTMLElement | null>(null);

    const handleLabelChange = (
        newSelectedFeedbackType: AnomalyFeedbackType
    ): void => {
        if (
            newSelectedFeedbackType &&
            newSelectedFeedbackType !== currentlySelected
        ) {
            showDialog({
                type: DialogType.ALERT,
                contents: t("message.change-confirmation-to", {
                    value: `"${ALL_OPTIONS_TO_DESCRIPTIONS[newSelectedFeedbackType]}"`,
                }),
                okButtonText: t("label.change"),
                cancelButtonText: t("label.cancel"),
                onOk: () =>
                    handleFeedbackChangeOk(
                        newSelectedFeedbackType,
                        anomalyFeedback.comment
                    ),
            });
        }
    };

    const handleCommentUpdateClick = (): void => {
        showDialog({
            type: DialogType.CUSTOM,
            headerText: t("label.update-entity", {
                entity: t("label.comment"),
            }),
            cancelButtonText: t("label.cancel"),
            contents: (
                <>
                    {updateHasError && (
                        <Box
                            color="warning.main"
                            marginBottom="10px"
                            marginTop="-15px"
                        >
                            {t("message.changes-not-saved")}
                        </Box>
                    )}
                    <TextField
                        fullWidth
                        multiline
                        defaultValue={modifiedFeedbackComment}
                        inputRef={commentRef}
                        minRows={3}
                        name="comment"
                    />
                </>
            ),
            okButtonText: t("label.change"),
            onOk: () =>
                handleFeedbackChangeOk(
                    currentlySelected,
                    commentRef.current?.value || modifiedFeedbackComment
                ),
        });
    };

    const handleFeedbackChangeOk = (
        feedbackType: AnomalyFeedbackType,
        comment: string
    ): void => {
        const updateRequestPayload = {
            ...anomalyFeedback,
            type: feedbackType,
            comment,
        };

        // Placeholder for modified comment in case request fails
        setModifiedFeedbackComment(comment);

        updateAnomalyFeedback(anomalyId, updateRequestPayload)
            .then(() => {
                notify(
                    NotificationTypeV1.Success,
                    t("message.update-success", {
                        entity: t("label.anomaly-feedback"),
                    })
                );
                setUpdateHasError(false);
                setCurrentlySelected(feedbackType);
            })
            .catch((error: AxiosError) => {
                const errMessages = getErrorMessages(error);

                setUpdateHasError(true);

                isEmpty(errMessages)
                    ? notify(
                          NotificationTypeV1.Error,

                          t("message.update-error", {
                              entity: t("label.anomaly-feedback"),
                          })
                      )
                    : errMessages.map((err) =>
                          notify(
                              NotificationTypeV1.Error,
                              err.message,
                              err.details
                          )
                      );
            });
    };

    useEffect(() => {
        setCurrentlySelected(anomalyFeedback.type);
        setModifiedFeedbackComment(anomalyFeedback.comment);
    }, [anomalyFeedback]);

    const shortcutCreateMenuItems = Object.keys(
        ALL_OPTIONS_TO_DESCRIPTIONS
    ).map((optionKey: string) => ({
        id: optionKey,
        text: ALL_OPTIONS_TO_DESCRIPTIONS[optionKey],
    }));

    return (
        <>
            <Grid container alignItems="center">
                <Grid item>
                    {/** The single dropdown should trigger the menu open */}
                    <ButtonGroup
                        color="primary"
                        size="small"
                        variant="outlined"
                        onClick={(e: MouseEvent<HTMLElement>) =>
                            setFeedbackMenuAnchorEl(e.currentTarget)
                        }
                    >
                        <Button color="primary" variant="outlined">
                            {ALL_OPTIONS_WITH_NO_FEEDBACK[currentlySelected]}
                        </Button>
                        <Button
                            color="primary"
                            variant={
                                feedbackMenuAnchorEl ? "contained" : "outlined"
                            }
                        >
                            <KeyboardArrowDownIcon />
                        </Button>
                    </ButtonGroup>
                    <DropdownMenuV1
                        anchorEl={feedbackMenuAnchorEl}
                        className="dropdown-button-v1-menu"
                        dropdownMenuItems={shortcutCreateMenuItems}
                        open={Boolean(feedbackMenuAnchorEl)}
                        onClick={(menuItemId: number | string): void => {
                            !!handleFeedbackChangeOk &&
                                handleLabelChange(
                                    menuItemId as unknown as AnomalyFeedbackType
                                );
                            setFeedbackMenuAnchorEl(null);
                        }}
                        onClose={() => setFeedbackMenuAnchorEl(null)}
                    />
                </Grid>
                {!hideComment && (
                    <Grid item>
                        <TooltipV1
                            placement="top"
                            title={
                                anomalyFeedback.comment ||
                                t("message.click-to-add-comment")
                            }
                        >
                            <Button
                                color="primary"
                                size="small"
                                variant="outlined"
                                onClick={handleCommentUpdateClick}
                            >
                                <CommentIcon />
                            </Button>
                        </TooltipV1>
                    </Grid>
                )}
            </Grid>
        </>
    );
};
