/**
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import { Icon } from "@iconify/react";
import {
    Box,
    Button,
    Card,
    CardContent,
    Divider,
    Grid,
    TextField,
    Typography,
} from "@material-ui/core";
import { cloneDeep } from "lodash";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { LocalThemeProviderV1 } from "../../../../platform/components";
import { lightV1 } from "../../../../platform/utils";
import { validateEmail } from "../../../../utils/validation/validation.util";
import { EditableList } from "../../../editable-list/editable-list.component";
import { SendgridEmailProps } from "./sendgrid-email.interfaces";

export const SendgridEmail: FunctionComponent<SendgridEmailProps> = ({
    configuration,
    onSpecChange,
    onDeleteClick,
}) => {
    const { t } = useTranslation();

    const handleEmailListChange = (emails: string[]): void => {
        const copied = cloneDeep(configuration);
        copied.params.emailRecipients.to = emails;
        onSpecChange(copied);
    };

    return (
        <Card elevation={2}>
            <CardContent>
                <Grid container justifyContent="space-between">
                    <Grid item>
                        <Typography variant="h6">
                            <Icon height={12} icon="carbon:email" />{" "}
                            {t("label.email")}
                        </Typography>
                    </Grid>
                    <Grid item>
                        <Box textAlign="right">
                            <LocalThemeProviderV1
                                primary={lightV1.palette.error}
                            >
                                <Button
                                    color="primary"
                                    data-testid="email-delete-btn"
                                    onClick={onDeleteClick}
                                >
                                    {t("label.delete")}
                                </Button>
                            </LocalThemeProviderV1>
                        </Box>
                    </Grid>
                </Grid>
            </CardContent>
            <CardContent>
                <EditableList
                    addButtonLabel={t("label.add")}
                    inputLabel={t("label.add-entity", {
                        entity: t("label.email"),
                    })}
                    list={configuration.params.emailRecipients.to || []}
                    validateFn={validateEmail}
                    onChange={handleEmailListChange}
                />
                <Grid container alignItems="center">
                    <Grid item xs={12}>
                        <Box marginBottom={1} marginTop={1}>
                            <Divider />
                        </Box>
                    </Grid>
                    <Grid item xs={12}>
                        <Typography variant="subtitle2">
                            {t("label.optional-overrides")}
                        </Typography>
                    </Grid>
                    <Grid item xs={1}>
                        <Typography variant="subtitle2">
                            {t("label.sendgrid-api-key")}
                        </Typography>
                    </Grid>
                    <Grid item xs={11}>
                        <TextField
                            fullWidth
                            data-testid="api-key-input-container"
                            value={configuration.params.apiKey}
                            variant="outlined"
                            onChange={(e) => {
                                const copied = cloneDeep(configuration);
                                copied.params.apiKey = e.currentTarget.value;
                                onSpecChange(copied);
                            }}
                        />
                    </Grid>
                </Grid>
                <Grid container alignItems="center">
                    <Grid item xs={1}>
                        <Typography variant="subtitle2">
                            {t("label.from")}
                        </Typography>
                    </Grid>
                    <Grid item xs={11}>
                        <TextField
                            fullWidth
                            data-testid="from-input-container"
                            value={configuration.params.emailRecipients.from}
                            variant="outlined"
                            onChange={(e) => {
                                const copied = cloneDeep(configuration);
                                copied.params.emailRecipients.from =
                                    e.currentTarget.value;
                                onSpecChange(copied);
                            }}
                        />
                    </Grid>
                </Grid>
            </CardContent>
        </Card>
    );
};
