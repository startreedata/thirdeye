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
import React, { FunctionComponent, useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { AppLoadingIndicatorV1 } from "../../platform/components";
import { useGetAlert } from "../../rest/alerts/alerts.actions";
import type { EditableAlert } from "../../rest/dto/alert.interfaces";
import type { AlertsViewPageParams } from "../alerts-view-page/alerts-view-page.interfaces";
import { AlertsCreateBasePage } from "./alerts-create-base-page.component";
import { createAlertCopy } from "./alerts-create-page.utils";

export const AlertsCreateCopyPage: FunctionComponent = () => {
    const { id: alertId } = useParams<AlertsViewPageParams>();
    const [isLoading, setIsLoading] = useState(true);
    const [alertToCopy, setAlertToCopy] = useState<EditableAlert>();
    const { getAlert } = useGetAlert();

    useEffect(() => {
        getAlert(Number(alertId)).then((alert) => {
            if (alert) {
                const editableVersion = createAlertCopy(alert);

                setAlertToCopy(editableVersion);
                setIsLoading(false);
            }
        });
    }, []);

    if (isLoading) {
        return <AppLoadingIndicatorV1 />;
    }

    return (
        <AlertsCreateBasePage
            startingAlertConfiguration={alertToCopy as EditableAlert}
        />
    );
};
