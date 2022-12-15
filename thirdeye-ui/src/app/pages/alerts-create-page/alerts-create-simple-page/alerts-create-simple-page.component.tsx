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
import { Grid } from "@material-ui/core";
import React, { FunctionComponent, useEffect } from "react";
import { useOutletContext } from "react-router-dom";
import { AlertDetails } from "../../../components/alert-wizard-v2/alert-details/alert-details.component";
import { AlertNotifications } from "../../../components/alert-wizard-v2/alert-notifications/alert-notifications.component";
import { AlertTemplate } from "../../../components/alert-wizard-v2/alert-template/alert-template.component";
import { PageContentsGridV1 } from "../../../platform/components";
import { AlertEditPageOutletContextProps } from "../../alerts-update-page/alerts-update-page.interfaces";

export const AlertsCreateSimplePage: FunctionComponent = () => {
    const {
        alert,
        handleAlertPropertyChange: onAlertPropertyChange,
        handleSubscriptionGroupChange: onSubscriptionGroupsChange,
        selectedAlertTemplate,
        setSelectedAlertTemplate,
        alertTemplateOptions,
        setShowBottomBar,
    } = useOutletContext<AlertEditPageOutletContextProps>();

    useEffect(() => {
        setShowBottomBar(true);
    }, []);

    return (
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
            <Grid item xs={12}>
                <AlertNotifications
                    alert={alert}
                    initiallySelectedSubscriptionGroups={[]}
                    onSubscriptionGroupsChange={onSubscriptionGroupsChange}
                />
            </Grid>
        </PageContentsGridV1>
    );
};
