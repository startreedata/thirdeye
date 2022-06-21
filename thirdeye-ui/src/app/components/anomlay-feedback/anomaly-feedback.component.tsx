import {
    Box,
    Button,
    Grid,
    MenuItem,
    Popover,
    TextField,
} from "@material-ui/core";
import ArrowDropDownIcon from "@material-ui/icons/ArrowDropDown";
import ArrowDropUpIcon from "@material-ui/icons/ArrowDropUp";
import { AxiosError } from "axios";
import { isEmpty } from "lodash";
import React, { FunctionComponent, useEffect, useRef, useState } from "react";
import { useTranslation } from "react-i18next";
import {
    NotificationTypeV1,
    PageContentsCardV1,
    useDialogProviderV1,
    useNotificationProviderV1,
} from "../../platform/components";
import { DialogType } from "../../platform/components/dialog-provider-v1/dialog-provider-v1.interfaces";
import { updateAnomalyFeedback } from "../../rest/anomalies/anomalies.rest";
import { AnomalyFeedbackType } from "../../rest/dto/anomaly.interfaces";
import { getErrorMessages } from "../../utils/rest/rest.util";
import { AnomalyFeedbackProps } from "./anomaly-feedback.interfaces";

const OPTION_TO_DESCRIPTIONS = {
    [AnomalyFeedbackType.ANOMALY.valueOf()]: "Anomaly - unexpected",
    [AnomalyFeedbackType.ANOMALY_EXPECTED.valueOf()]:
        "Anomaly - Expected temporary change",
    [AnomalyFeedbackType.ANOMALY_NEW_TREND.valueOf()]:
        "Anomaly - Permanent change",
    [AnomalyFeedbackType.NOT_ANOMALY.valueOf()]: "Not an anomaly",
    [AnomalyFeedbackType.NO_FEEDBACK.valueOf()]: "No feedback",
};

export const AnomalyFeedback: FunctionComponent<AnomalyFeedbackProps> = ({
    anomalyId,
    anomalyFeedback,
    className,
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

    const [anchorEl, setAnchorEl] = React.useState<HTMLButtonElement | null>(
        null
    );

    const handleClick = (event: React.MouseEvent<HTMLButtonElement>): void => {
        setAnchorEl(event.currentTarget);
    };

    const handleClose = (): void => {
        setAnchorEl(null);
    };

    const handleLabelChange = (
        event: React.ChangeEvent<{ value: unknown }>
    ): void => {
        const newSelectedFeedbackType = event.target
            .value as string as AnomalyFeedbackType;

        if (
            newSelectedFeedbackType &&
            newSelectedFeedbackType !== currentlySelected
        ) {
            showDialog({
                type: DialogType.ALERT,
                contents: t("message.change-confirmation-to", {
                    value: `"${OPTION_TO_DESCRIPTIONS[newSelectedFeedbackType]}"`,
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
                        name="comment"
                        rows={3}
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
                          notify(NotificationTypeV1.Error, err)
                      );
            })
            .finally(() => {
                handleClose();
            });
    };

    useEffect(() => {
        setCurrentlySelected(anomalyFeedback.type);
        setModifiedFeedbackComment(anomalyFeedback.comment);
    }, [anomalyFeedback]);

    const open = Boolean(anchorEl);

    return (
        <>
            <Box marginTop="10px">
                <Button
                    endIcon={open ? <ArrowDropUpIcon /> : <ArrowDropDownIcon />}
                    size="small"
                    variant="text"
                    onClick={handleClick}
                >
                    Is this an anomaly?
                </Button>
            </Box>

            <Popover
                anchorEl={anchorEl}
                anchorOrigin={{
                    vertical: "bottom",
                    horizontal: "left",
                }}
                id="anomaly-feedback"
                open={open}
                transformOrigin={{
                    vertical: "top",
                    horizontal: "left",
                }}
                onClose={handleClose}
            >
                <PageContentsCardV1 className={className}>
                    <Grid container direction="column" spacing={2}>
                        <Grid item>
                            <TextField
                                fullWidth
                                select
                                id="anomaly-feedback-select"
                                value={currentlySelected}
                                onChange={handleLabelChange}
                            >
                                {Object.keys(OPTION_TO_DESCRIPTIONS).map(
                                    (optionKey: string) => (
                                        <MenuItem
                                            key={optionKey}
                                            value={optionKey}
                                        >
                                            {OPTION_TO_DESCRIPTIONS[optionKey]}
                                        </MenuItem>
                                    )
                                )}
                            </TextField>
                        </Grid>

                        <Grid item>
                            <Button onClick={handleCommentUpdateClick}>
                                View / Edit comment
                            </Button>
                        </Grid>
                    </Grid>
                </PageContentsCardV1>
            </Popover>
        </>
    );
};
