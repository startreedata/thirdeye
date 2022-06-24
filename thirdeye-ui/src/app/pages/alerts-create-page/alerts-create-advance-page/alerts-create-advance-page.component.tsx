import { Box, Grid } from "@material-ui/core";
import React, { FunctionComponent, useEffect, useMemo, useState } from "react";
import { useOutletContext } from "react-router-dom";
import { AlertJson } from "../../../components/alert-wizard-v2/alert-json/alert-json.component";
import { AlertNotifications } from "../../../components/alert-wizard-v2/alert-notifications/alert-notifications.component";
import {
    findRequiredFields,
    hasRequiredPropertyValuesSet,
} from "../../../components/alert-wizard-v2/alert-template/alert-template.utils";
import { PreviewChart } from "../../../components/alert-wizard-v2/alert-template/preview-chart/preview-chart.component";
import { MessageDisplayState } from "../../../components/alert-wizard-v2/alert-template/preview-chart/preview-chart.interfaces";
import { PageContentsCardV1 } from "../../../platform/components";
import { AlertTemplate as AlertTemplateType } from "../../../rest/dto/alert-template.interfaces";
import { EditableAlert } from "../../../rest/dto/alert.interfaces";
import { SubscriptionGroup } from "../../../rest/dto/subscription-group.interfaces";

export const AlertsCreateAdvancePage: FunctionComponent = () => {
    const [isRequiredPropertyValuesSet, setIsRequiredPropertyValuesSet] =
        useState(false);
    const [
        alert,
        onAlertPropertyChange,
        ,
        onSubscriptionGroupsChange,
        selectedAlertTemplate,
    ] =
        useOutletContext<
            [
                EditableAlert,
                (contents: Partial<EditableAlert>) => void,
                SubscriptionGroup[],
                (groups: SubscriptionGroup[]) => void,
                AlertTemplateType
            ]
        >();

    const requiredFields = useMemo(() => {
        if (selectedAlertTemplate) {
            return findRequiredFields(selectedAlertTemplate);
        }

        return [];
    }, [selectedAlertTemplate]);

    useEffect(() => {
        const isValid =
            !!selectedAlertTemplate &&
            hasRequiredPropertyValuesSet(
                requiredFields,
                alert.templateProperties,
                selectedAlertTemplate.defaultProperties || {}
            );

        setIsRequiredPropertyValuesSet(isValid);
    }, [selectedAlertTemplate, alert]);

    return (
        <>
            <Grid item xs={12}>
                <PageContentsCardV1>
                    <AlertJson
                        alert={alert}
                        onAlertPropertyChange={onAlertPropertyChange}
                    />
                    <Box marginTop={2}>
                        <PreviewChart
                            alert={alert}
                            displayState={
                                selectedAlertTemplate
                                    ? isRequiredPropertyValuesSet
                                        ? MessageDisplayState.GOOD_TO_PREVIEW
                                        : MessageDisplayState.FILL_TEMPLATE_PROPERTY_VALUES
                                    : MessageDisplayState.SELECT_TEMPLATE
                            }
                        />
                    </Box>
                </PageContentsCardV1>
            </Grid>
            <Grid item xs={12}>
                <AlertNotifications
                    alert={alert}
                    onSubscriptionGroupsChange={onSubscriptionGroupsChange}
                />
            </Grid>
        </>
    );
};
