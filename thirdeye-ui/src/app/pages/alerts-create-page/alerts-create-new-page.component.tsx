import React, { FunctionComponent } from "react";
import { createNewStartingAlert } from "../../components/alert-wizard-v2/alert-template/alert-template.utils";
import { AlertsCreateBasePage } from "./alerts-create-base-page.component";

export const AlertsCreateNewPage: FunctionComponent = () => {
    return (
        <AlertsCreateBasePage
            startingAlertConfiguration={createNewStartingAlert()}
        />
    );
};
