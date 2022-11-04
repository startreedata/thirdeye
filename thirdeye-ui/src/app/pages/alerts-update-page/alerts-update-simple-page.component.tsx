import { Grid } from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { useOutletContext } from "react-router-dom";
import { AlertDetails } from "../../components/alert-wizard-v2/alert-details/alert-details.component";
import { AlertNotifications } from "../../components/alert-wizard-v2/alert-notifications/alert-notifications.component";
import { AlertTemplate } from "../../components/alert-wizard-v2/alert-template/alert-template.component";
import { AlertTemplate as AlertTemplateType } from "../../rest/dto/alert-template.interfaces";
import { EditableAlert } from "../../rest/dto/alert.interfaces";
import { SubscriptionGroup } from "../../rest/dto/subscription-group.interfaces";

export const AlertsUpdateSimplePage: FunctionComponent = () => {
    const [
        alert,
        onAlertPropertyChange,
        selectedSubscriptionGroups,
        onSubscriptionGroupsChange,
        selectedAlertTemplate,
        setSelectedAlertTemplate,
        alertTemplateOptions,
    ] =
        useOutletContext<
            [
                EditableAlert,
                (contents: Partial<EditableAlert>) => void,
                SubscriptionGroup[],
                (groups: SubscriptionGroup[]) => void,
                AlertTemplateType,
                (newAlertTemplate: AlertTemplateType | null) => void,
                AlertTemplateType[]
            ]
        >();

    return (
        <>
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
            <Grid item xs={12}>
                <AlertNotifications
                    alert={alert}
                    initiallySelectedSubscriptionGroups={
                        selectedSubscriptionGroups
                    }
                    onSubscriptionGroupsChange={onSubscriptionGroupsChange}
                />
            </Grid>
        </>
    );
};
