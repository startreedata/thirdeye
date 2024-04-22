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
import { Box, Divider, Grid, Link, Typography } from "@material-ui/core";
import InfoIcon from "@material-ui/icons/Info";
import Alert from "@material-ui/lab/Alert";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { JSONEditorV1 } from "../../../platform/components";
import { EditableAlert } from "../../../rest/dto/alert.interfaces";
import { THIRDEYE_DOC_LINK } from "../../../utils/constants/constants.util";
import { getAlertTemplatesAllPath } from "../../../utils/routes/routes.util";
import { validateJSON } from "../../../utils/validation/validation.util";
import { NavigateAlertCreationFlowsDropdown } from "../../alert-wizard-v3/navigate-alert-creation-flows-dropdown/navigate-alert-creation-flows-dropdown";
import { useAlertWizardV2Styles } from "../alert-wizard-v2.styles";
import { AlertJsonProps } from "./alert-json.interfaces";

export const AlertJson: FunctionComponent<AlertJsonProps> = ({
    alert,
    onAlertPropertyChange,
    setIsAlertValid,
}) => {
    const { t } = useTranslation();
    const classes = useAlertWizardV2Styles();
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
                        <Typography variant="h5">
                            {t(
                                "label.advanced-template-configuration-json-editor"
                            )}
                        </Typography>
                    </Grid>
                    <Grid item>
                        <NavigateAlertCreationFlowsDropdown />
                    </Grid>
                </Grid>
            </Grid>

            <Grid item xs={12}>
                <Alert
                    className={classes.infoAlert}
                    icon={<InfoIcon />}
                    severity="info"
                >
                    {t("message.changes-added-to-template-properties")}
                    <Link href={getAlertTemplatesAllPath()} target="_blank">
                        {t("label.template-configuration").toLowerCase()}
                    </Link>
                </Alert>
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
                    . {t("message.properties-and-values-may-differ")}
                </Typography>
            </Grid>

            <Grid item xs={12}>
                <Box paddingBottom={2} paddingTop={2}>
                    <Divider />
                </Box>
            </Grid>

            <Grid item xs={12}>
                <JSONEditorV1<EditableAlert>
                    hideValidationSuccessIcon
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
