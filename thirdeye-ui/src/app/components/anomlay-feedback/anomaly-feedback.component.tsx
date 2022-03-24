import {
    Button,
    Card,
    CardContent,
    Grid,
    MenuItem,
    TextField,
} from "@material-ui/core";
import { AxiosError } from "axios";
import { isEmpty } from "lodash";
import React, { FunctionComponent, useRef, useState } from "react";
import { useTranslation } from "react-i18next";
import {
    NotificationTypeV1,
    useNotificationProviderV1,
} from "../../platform/components";
import { updateAnomalyFeedback } from "../../rest/anomalies/anomalies.rest";
import { AnomalyFeedbackType } from "../../rest/dto/anomaly.interfaces";
import { getErrorMessages } from "../../utils/rest/rest.util";
import { useDialog } from "../dialogs/dialog-provider/dialog-provider.component";
import { DialogType } from "../dialogs/dialog-provider/dialog-provider.interfaces";
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
    const [feedbackComment, setFeedbackComment] = useState(
        anomalyFeedback.comment
    );
    const { showDialog } = useDialog();
    const { notify } = useNotificationProviderV1();
    const { t } = useTranslation();
    const commentRef = useRef<HTMLInputElement>();

    const handleChange = (
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
                text: t("message.change-confirmation-to", {
                    value: `"${OPTION_TO_DESCRIPTIONS[newSelectedFeedbackType]}"`,
                }),
                okButtonLabel: t("label.change"),
                onOk: () =>
                    handleFeedbackChangeOk(
                        newSelectedFeedbackType,
                        commentRef.current?.value || anomalyFeedback.comment
                    ),
            });
        }
    };

    const handleCommentUpdate = (): void => {
        showDialog({
            type: DialogType.CUSTOM,
            title: "Update comment",
            children: (
                <TextField
                    fullWidth
                    multiline
                    defaultValue={feedbackComment}
                    inputRef={commentRef}
                    name="comment"
                    rows={3}
                />
            ),
            okButtonLabel: t("label.change"),
            onOk: () =>
                handleFeedbackChangeOk(
                    currentlySelected,
                    commentRef.current?.value || feedbackComment
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
        updateAnomalyFeedback(anomalyId, updateRequestPayload)
            .then(() => {
                notify(
                    NotificationTypeV1.Success,
                    t("message.update-success", {
                        entity: t("label.anomaly-feedback"),
                    })
                );
                setCurrentlySelected(feedbackType);
                setFeedbackComment(comment);
            })
            .catch((error: AxiosError) => {
                // Reset comment on error
                setFeedbackComment(anomalyFeedback.comment);

                const errMessages = getErrorMessages(error);

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
            });
    };

    return (
        <Card className={className} variant="outlined">
            <CardContent>
                <Grid container>
                    <Grid item xs={12}>
                        <label>
                            <strong>Is this an anomaly?</strong>
                        </label>
                    </Grid>
                    <Grid item xs={12}>
                        <TextField
                            fullWidth
                            select
                            id="anomaly-feedback-select"
                            value={currentlySelected}
                            onChange={handleChange}
                        >
                            {Object.keys(OPTION_TO_DESCRIPTIONS).map(
                                (optionKey: string) => (
                                    <MenuItem key={optionKey} value={optionKey}>
                                        {OPTION_TO_DESCRIPTIONS[optionKey]}
                                    </MenuItem>
                                )
                            )}
                        </TextField>
                    </Grid>
                    <Grid item xs={12}>
                        <Button onClick={handleCommentUpdate}>
                            View / Edit comment
                        </Button>
                    </Grid>
                </Grid>
            </CardContent>
        </Card>
    );
};
