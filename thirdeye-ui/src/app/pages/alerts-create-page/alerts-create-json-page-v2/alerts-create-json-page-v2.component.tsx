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
import React, { FunctionComponent, useMemo } from "react";
import { useOutletContext } from "react-router-dom";
import { AlertJson } from "../../../components/alert-wizard-v2/alert-json-v2/alert-json.component";
import {
    determinePropertyFieldConfiguration,
    hasRequiredPropertyValuesSet,
} from "../../../components/alert-wizard-v2/alert-template/alert-template.utils";
import { PreviewChart } from "../../../components/alert-wizard-v2/alert-template/preview-chart/preview-chart.component";
import {
    PageContentsCardV1,
    PageContentsGridV1,
} from "../../../platform/components";
import { AlertsSimpleAdvancedJsonContainerPageOutletContextProps } from "../../alerts-edit-create-common/alerts-edit-create-common-page.interfaces";

export const AlertsCreateJSONPage: FunctionComponent = () => {
    const {
        alert,
        handleAlertPropertyChange: onAlertPropertyChange,
        selectedAlertTemplate,
    } = useOutletContext<AlertsSimpleAdvancedJsonContainerPageOutletContextProps>();

    const [isAlertValid, setIsAlertValid] = React.useState(false);

    const availableFields = useMemo(() => {
        if (selectedAlertTemplate) {
            return determinePropertyFieldConfiguration(selectedAlertTemplate);
        }

        return [];
    }, [selectedAlertTemplate]);

    const areBasicFieldsFilled = useMemo(() => {
        return (
            !!selectedAlertTemplate &&
            hasRequiredPropertyValuesSet(
                availableFields,
                alert.templateProperties
            )
        );
    }, [availableFields, alert]);

    return (
        <>
            <PageContentsGridV1>
                <Grid item xs={12}>
                    <PageContentsCardV1>
                        <AlertJson
                            alert={alert}
                            setIsAlertValid={setIsAlertValid}
                            onAlertPropertyChange={onAlertPropertyChange}
                        />
                        <Box>
                            <PreviewChart
                                alert={alert}
                                disableReload={!isAlertValid}
                                hideCallToActionPrompt={areBasicFieldsFilled}
                                onAlertPropertyChange={onAlertPropertyChange}
                            />
                        </Box>
                    </PageContentsCardV1>
                </Grid>
            </PageContentsGridV1>
        </>
    );
};
