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
import {
    Box,
    Button,
    Checkbox,
    FormControlLabel,
    Grid,
    Typography,
} from "@material-ui/core";
import React, { FunctionComponent, useState } from "react";
import { useTranslation } from "react-i18next";
import {
    HelpLinkIconV1,
    JSONEditorV1,
    PageContentsCardV1,
    PageContentsGridV1,
    TooltipV1,
} from "../../platform/components";
import { Datasource } from "../../rest/dto/datasource.interfaces";
import { THIRDEYE_DOC_LINK } from "../../utils/constants/constants.util";
import { createDefaultDatasource } from "../../utils/datasources/datasources.util";
import { DatasourceWizardProps } from "./datasource-wizard.interfaces";

export const DatasourceWizard: FunctionComponent<DatasourceWizardProps> = ({
    submitBtnLabel,
    datasource,
    onCancel,
    onSubmit,
    isCreate,
}) => {
    const [editedDatasource, setEditedDatasource] = useState<Datasource>(
        datasource || createDefaultDatasource()
    );

    const [autoOnboard, setAutoOnboard] = useState(false);
    const { t } = useTranslation();

    const onDatasourceConfigurationChange = (value: string): void => {
        setEditedDatasource(JSON.parse(value));
    };

    const handleAutoOnboardChange = (
        _: React.ChangeEvent<HTMLInputElement>,
        checked: boolean
    ): void => {
        setAutoOnboard(checked);
    };

    const handleSubmitClick = (): void => {
        onSubmit && onSubmit(editedDatasource, autoOnboard);
    };

    return (
        <>
            <PageContentsGridV1>
                <Grid item xs={12}>
                    <PageContentsCardV1>
                        <Grid container>
                            {/* Step label */}
                            <Grid item sm={12}>
                                <Typography variant="h5">
                                    {t("label.datasource-configuration")}

                                    <TooltipV1
                                        placement="top"
                                        title={
                                            t(
                                                "label.view-configuration-docs"
                                            ) as string
                                        }
                                    >
                                        <span>
                                            <HelpLinkIconV1
                                                displayInline
                                                enablePadding
                                                externalLink
                                                href={`${THIRDEYE_DOC_LINK}/how-tos/database/`}
                                            />
                                        </span>
                                    </TooltipV1>
                                </Typography>
                            </Grid>

                            {/* Datasource configuration */}
                            {/* Datasource configuration editor */}
                            <Grid item sm={12}>
                                <JSONEditorV1<Datasource>
                                    hideValidationSuccessIcon
                                    value={editedDatasource}
                                    onChange={onDatasourceConfigurationChange}
                                />
                            </Grid>

                            {/* Dataset onboard check */}
                            {isCreate && (
                                <Grid item lg={4} md={5} sm={6} xs={12}>
                                    <FormControlLabel
                                        control={
                                            <Checkbox
                                                checked={autoOnboard}
                                                color="primary"
                                                onChange={
                                                    handleAutoOnboardChange
                                                }
                                            />
                                        }
                                        label={t("label.datasets-auto-onboard")}
                                    />
                                </Grid>
                            )}
                        </Grid>
                    </PageContentsCardV1>
                </Grid>
            </PageContentsGridV1>

            <Box textAlign="right" width="100%">
                <PageContentsCardV1>
                    <Grid container justifyContent="flex-end">
                        <Grid item>
                            <Button
                                color="secondary"
                                onClick={() => onCancel && onCancel()}
                            >
                                {t("label.cancel")}
                            </Button>
                        </Grid>
                        <Grid item>
                            <Button color="primary" onClick={handleSubmitClick}>
                                {submitBtnLabel}
                            </Button>
                        </Grid>
                    </Grid>
                </PageContentsCardV1>
            </Box>
        </>
    );
};
