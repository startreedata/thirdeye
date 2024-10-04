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
import { Box, Grid } from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { useOutletContext } from "react-router-dom";
import { AlertDetails } from "../../../components/alert-wizard-v2/alert-details/alert-details-v2.component";
import { AlertTemplate } from "../../../components/alert-wizard-v2/alert-template/alert-template-v2.component";
import { Portal } from "../../../components/portal/portal.component";
import { WizardBottomBar } from "../../../components/welcome-onboard-datasource/wizard-bottom-bar/wizard-bottom-bar.component";
import { PageContentsGridV1 } from "../../../platform/components";
import {
    AlertsSimpleAdvancedJsonContainerPageOutletContextProps,
    BOTTOM_BAR_ELEMENT_ID,
} from "../../alerts-edit-create-common/alerts-edit-create-common-page.interfaces";
import { easyAlertStyles } from "../alerts-create-easy-page/alerts-create-easy-page.styles";

export const AlertsCreateAdvancedPage: FunctionComponent = () => {
    const { t } = useTranslation();
    const classes = easyAlertStyles();

    const {
        alert,
        handleAlertPropertyChange: onAlertPropertyChange,
        selectedAlertTemplate,
        setSelectedAlertTemplate,
        alertTemplateOptions,
        isEditRequestInFlight,
        handleSubmitAlertClick,
        onPageExit,
    } = useOutletContext<AlertsSimpleAdvancedJsonContainerPageOutletContextProps>();

    return (
        <>
            <Box className={classes.backgroundContainer}>
                <PageContentsGridV1>
                    <Grid item xs={12}>
                        <AlertDetails
                            alert={alert}
                            onAlertPropertyChange={onAlertPropertyChange}
                        />
                    </Grid>
                    <Grid item xs={12}>
                        <AlertTemplate
                            alert={alert}
                            alertTemplateOptions={alertTemplateOptions}
                            selectedAlertTemplate={selectedAlertTemplate}
                            setSelectedAlertTemplate={setSelectedAlertTemplate}
                            onAlertPropertyChange={onAlertPropertyChange}
                        />
                    </Grid>
                </PageContentsGridV1>

                <Portal containerId={BOTTOM_BAR_ELEMENT_ID}>
                    <WizardBottomBar
                        doNotWrapInContainer
                        backButtonLabel={t("label.cancel")}
                        handleBackClick={onPageExit}
                        handleNextClick={() => handleSubmitAlertClick(alert)}
                        nextButtonIsDisabled={isEditRequestInFlight}
                        nextButtonLabel={t("label.create-entity", {
                            entity: t("label.alert"),
                        })}
                    />
                </Portal>
            </Box>
        </>
    );
};
