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
import { Box, Divider, Grid, Link, Typography } from "@material-ui/core";
import InfoIcon from "@material-ui/icons/Info";
import Alert from "@material-ui/lab/Alert";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { SAMPLE_ALERT_CONFIGURATION } from "../../../pages/alerts-create-page/alerts-create-advance-page/alerts-create-advance-page.util";
import {
    JSONEditorV1,
    useDialogProviderV1,
} from "../../../platform/components";
import { DialogType } from "../../../platform/components/dialog-provider-v1/dialog-provider-v1.interfaces";
import { EditableAlert } from "../../../rest/dto/alert.interfaces";
import { getAlertTemplatesAllPath } from "../../../utils/routes/routes.util";
import { useAlertWizardV2Styles } from "../alert-wizard-v2.styles";
import { AlertJsonProps } from "./alert-json.interfaces";

export const AlertJson: FunctionComponent<AlertJsonProps> = ({
    alert,
    onAlertPropertyChange,
}) => {
    const { showDialog } = useDialogProviderV1();
    const { t } = useTranslation();
    const classes = useAlertWizardV2Styles();

    const handleJSONChange = (json: string): void => {
        onAlertPropertyChange(JSON.parse(json), true);
    };

    const handleQuickStartClick = (): void => {
        showDialog({
            type: DialogType.CUSTOM,
            headerText: t("label.alert-json-template"),
            cancelButtonText: t("label.close"),
            contents: (
                <>
                    <Box paddingBottom={2}>
                        <Typography variant="body2">
                            {t("message.properties-and-values-may-differ")}
                        </Typography>
                    </Box>
                    <JSONEditorV1<EditableAlert>
                        hideValidationSuccessIcon
                        readOnly
                        value={SAMPLE_ALERT_CONFIGURATION}
                    />
                </>
            ),
            hideOkButton: true,
            width: "md",
        });
    };

    return (
        <Grid container item xs={12}>
            <Grid item xs={12}>
                <Box marginBottom={2}>
                    <Typography variant="h5">
                        {t("label.advanced-template-configuration-json-editor")}
                    </Typography>
                    <Typography variant="body2">
                        {t(
                            "message.attributes-different-from-simple-view-may-not-reflect"
                        )}
                    </Typography>
                </Box>
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
                <Box paddingTop={2}>
                    <Typography variant="body2">
                        {t("message.you-can-use-our")}
                        <Link
                            color="primary"
                            underline="always"
                            onClick={handleQuickStartClick}
                        >
                            “Quick Start complex Website JSON template”
                        </Link>
                        . {t("message.properties-and-values-may-differ")}
                    </Typography>
                </Box>
                <Box paddingBottom={2} paddingTop={2}>
                    <Divider />
                </Box>
            </Grid>

            <Grid item xs={12}>
                <JSONEditorV1<EditableAlert>
                    hideValidationSuccessIcon
                    value={alert}
                    onChange={handleJSONChange}
                />
            </Grid>
        </Grid>
    );
};
