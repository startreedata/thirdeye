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
    Button,
    Checkbox,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
    FormControl,
    FormControlLabel,
    Grid,
    ListItemText,
    MenuItem,
    Radio,
    RadioGroup,
    Select,
    TextField,
    Typography,
} from "@material-ui/core";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import {
    NotificationTypeV1,
    useNotificationProviderV1,
} from "../../../platform/components";
import { ActionStatus } from "../../../rest/actions.interfaces";
import { useUpdateAnomalyFeedback } from "../../../rest/anomalies/anomaly.actions";
import {
    AnomalyFeedback,
    AnomalyFeedbackType,
} from "../../../rest/dto/anomaly.interfaces";
import {
    ALL_OPTIONS_TO_DESCRIPTIONS,
    ANOMALY_OPTIONS_TO_DESCRIPTIONS,
} from "../../../utils/anomalies/anomalies.util";
import { notifyIfErrors } from "../../../utils/notifications/notifications.util";
import { AnomalyFeedbackModalProps } from "./anomaly-feedback-modal.interfaces";
import { REASONS } from "./anomaly-feedback-modal.utils";

export const AnomalyFeedbackModal: FunctionComponent<AnomalyFeedbackModalProps> =
    ({ anomalyId, anomalyFeedback, trigger, showNo, onFeedbackUpdate }) => {
        const { t } = useTranslation();
        const [isOpen, setIsOpen] = useState(false);
        const { notify } = useNotificationProviderV1();

        const { updateAnomalyFeedback, status, errorMessages } =
            useUpdateAnomalyFeedback();

        const [selectedFeedbackOption, setSelectedFeedbackOption] =
            useState<AnomalyFeedbackType>(() => {
                if (anomalyFeedback) {
                    return anomalyFeedback.type;
                }

                return AnomalyFeedbackType.ANOMALY.valueOf() as AnomalyFeedbackType;
            });

        const [localComment, setLocalComment] = useState<string>(() => {
            if (anomalyFeedback) {
                try {
                    const parsed = JSON.parse(anomalyFeedback.comment);

                    return parsed.comment;
                } catch {
                    return anomalyFeedback.comment;
                }
            }

            return "";
        });

        const [reasons, setReasons] = useState<string[]>(() => {
            if (anomalyFeedback) {
                try {
                    const parsed = JSON.parse(anomalyFeedback.comment);

                    return parsed.reasons;
                } catch {
                    return [];
                }
            }

            return [];
        });

        useEffect(() => {
            if (status === ActionStatus.Done) {
                notify(
                    NotificationTypeV1.Success,
                    t("message.update-success", {
                        entity: t("label.anomaly-feedback"),
                    })
                );

                setIsOpen(false);
            } else if (status === ActionStatus.Error) {
                notifyIfErrors(
                    status,
                    errorMessages,
                    notify,
                    t("message.update-error", {
                        entity: t("label.anomaly-feedback"),
                    })
                );
            }
        }, [status]);

        const handleSaveClick = (): void => {
            const feedbackObject: AnomalyFeedback = {
                type: selectedFeedbackOption,
                comment: JSON.stringify({
                    comment: localComment,
                    reasons: reasons,
                }),
            };

            if (anomalyFeedback?.id) {
                feedbackObject.id = anomalyFeedback.id;
            }

            updateAnomalyFeedback(anomalyId, feedbackObject).then(() => {
                onFeedbackUpdate(feedbackObject);
            });
        };

        const descriptionListToUse = showNo
            ? ALL_OPTIONS_TO_DESCRIPTIONS
            : ANOMALY_OPTIONS_TO_DESCRIPTIONS;

        return (
            <>
                {trigger(() => setIsOpen(true))}

                <Dialog open={isOpen} onClose={() => setIsOpen(false)}>
                    <DialogTitle>{t("label.anomaly-confirmation")}</DialogTitle>
                    <DialogContent dividers>
                        <Grid container>
                            <Grid item lg={4} md={4} sm={12} xs={12}>
                                <Typography variant="body2">
                                    {t("message.why-is-this-an-anomaly")}
                                </Typography>
                            </Grid>
                            <Grid item lg={8} md={8} sm={12} xs={12}>
                                <RadioGroup
                                    value={selectedFeedbackOption}
                                    onChange={(_, selected) =>
                                        setSelectedFeedbackOption(
                                            selected as AnomalyFeedbackType
                                        )
                                    }
                                >
                                    {Object.keys(descriptionListToUse).map(
                                        (k) => {
                                            return (
                                                <FormControlLabel
                                                    control={<Radio />}
                                                    key={k}
                                                    label={
                                                        ALL_OPTIONS_TO_DESCRIPTIONS[
                                                            k
                                                        ]
                                                    }
                                                    value={k}
                                                />
                                            );
                                        }
                                    )}
                                </RadioGroup>
                            </Grid>
                        </Grid>
                        <Grid container>
                            <Grid item lg={4} md={4} sm={12} xs={12}>
                                <Typography variant="body2">
                                    {t("message.what-are-the-reasons")}
                                </Typography>
                                <Typography variant="caption">
                                    ({t("label.optional")})
                                </Typography>
                            </Grid>
                            <Grid item lg={8} md={8} sm={12} xs={12}>
                                <FormControl fullWidth variant="outlined">
                                    <Select
                                        autoWidth
                                        displayEmpty
                                        multiple
                                        renderValue={(selected) =>
                                            (selected as string[]).length > 0
                                                ? (selected as string[]).join(
                                                      ", "
                                                  )
                                                : t(
                                                      "message.click-to-select-reasons"
                                                  )
                                        }
                                        value={reasons}
                                        onChange={(
                                            event: React.ChangeEvent<{
                                                value: unknown;
                                            }>
                                        ) => {
                                            setReasons(
                                                event.target.value as string[]
                                            );
                                        }}
                                    >
                                        {REASONS.map((name) => (
                                            <MenuItem key={name} value={name}>
                                                <Checkbox
                                                    checked={
                                                        reasons.indexOf(name) >
                                                        -1
                                                    }
                                                />
                                                <ListItemText primary={name} />
                                            </MenuItem>
                                        ))}
                                    </Select>
                                </FormControl>
                            </Grid>
                        </Grid>
                        <Grid container>
                            <Grid item lg={4} md={4} sm={12} xs={12}>
                                <Typography variant="body2">
                                    {t("label.comments")}
                                </Typography>
                                <Typography variant="caption">
                                    ({t("label.optional")})
                                </Typography>
                            </Grid>
                            <Grid item lg={8} md={8} sm={12} xs={12}>
                                <TextField
                                    fullWidth
                                    multiline
                                    defaultValue={localComment}
                                    minRows={3}
                                    onChange={(e) =>
                                        setLocalComment(e.target.value)
                                    }
                                />
                            </Grid>
                        </Grid>
                    </DialogContent>
                    <DialogActions>
                        <Button
                            color="primary"
                            disabled={status === ActionStatus.Working}
                            onClick={() => setIsOpen(false)}
                        >
                            {t("label.cancel")}
                        </Button>
                        <Button
                            color="primary"
                            disabled={status === ActionStatus.Working}
                            onClick={handleSaveClick}
                        >
                            {t("label.save")}
                        </Button>
                    </DialogActions>
                </Dialog>
            </>
        );
    };
