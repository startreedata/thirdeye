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
import { Box, Button, Grid, Link, Typography } from "@material-ui/core";
import { Icon } from "@iconify/react";

import InfoIcon from "@material-ui/icons/Info";
import Alert from "@material-ui/lab/Alert";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { easyAlertStyles } from "../../../pages/alerts-create-page/alerts-create-easy-page/alerts-create-easy-page.styles";
import {
    JSONEditorV2,
    PageHeaderActionsV1,
} from "../../../platform/components";
import { EditableAlert } from "../../../rest/dto/alert.interfaces";
import { THIRDEYE_DOC_LINK } from "../../../utils/constants/constants.util";
import { validateJSON } from "../../../utils/validation/validation.util";
import { HelpDrawerV1 } from "../../help-drawer-v1/help-drawer-v1.component";
import { AlertJsonProps } from "./alert-json.interfaces";

export const AlertJson: FunctionComponent<AlertJsonProps> = ({
    alert,
    onAlertPropertyChange,
    setIsAlertValid,
}) => {
    const classes = easyAlertStyles();

    const { t } = useTranslation();
    // proxy the given alert so users can freely type in the json editor
    const [initialAlert] = useState<EditableAlert>(alert);

    const [editedAlert, setEditedAlert] = useState<string>(
        JSON.stringify(alert)
    );

    const handleJSONChange = (newJson: string): void => {
        setEditedAlert(newJson);

        try {
            onAlertPropertyChange(JSON.parse(newJson), true);
        } catch {
            // do nothing if invalid JSON string
        }
    };

    const isAlertValid = validateJSON(editedAlert).valid;

    useEffect(() => {
        setIsAlertValid(isAlertValid);
    }, [editedAlert]);

    return (
        <Grid container>
            <Grid item xs={12}>
                <Grid
                    container
                    alignContent="center"
                    justifyContent="space-between"
                >
                    <Grid item>
                        <Typography className={classes.header} variant="h5">
                            {t("label.advanced-json-editor")}
                        </Typography>
                    </Grid>
                    <PageHeaderActionsV1>
                        <HelpDrawerV1
                            title={`${t("label.need-help")}?`}
                            trigger={(handleOpen) => (
                                <Button
                                    className={classes.infoButton}
                                    color="primary"
                                    size="small"
                                    variant="outlined"
                                    onClick={handleOpen}
                                >
                                    <Box component="span" mr={1}>
                                        {t("label.need-help")}
                                    </Box>
                                    <Box component="span" display="flex">
                                        <Icon
                                            fontSize={24}
                                            icon="mdi:question-mark-circle-outline"
                                        />
                                    </Box>
                                </Button>
                            )}
                        />
                    </PageHeaderActionsV1>
                </Grid>
            </Grid>

            <Grid item xs={12}>
                <Typography variant="body2">
                    {t("message.you-can-use-our")}
                    <Link
                        color="primary"
                        href={`${THIRDEYE_DOC_LINK}/getting-started/alert-create-examples`}
                        target="_blank"
                        underline="always"
                    >
                        {t("label.example-alert-configurations")}
                    </Link>
                    .
                </Typography>
                <Typography variant="body2">
                    {t("message.properties-and-values-may-differ")}
                </Typography>
            </Grid>

            <Grid item xs={12}>
                <JSONEditorV2<EditableAlert>
                    hideValidationSuccessIcon
                    actions={[]}
                    value={initialAlert}
                    onChange={handleJSONChange}
                />
            </Grid>

            {!isAlertValid && (
                <Grid item xs={12}>
                    <Alert
                        icon={<InfoIcon />}
                        severity="error"
                        variant="outlined"
                    >
                        {t(
                            "message.invalid-alert-configuration-please-fix-the-configuration"
                        )}
                    </Alert>
                </Grid>
            )}
        </Grid>
    );
};
