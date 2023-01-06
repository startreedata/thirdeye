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
import { default as React, FunctionComponent, useEffect } from "react";
import { useOutletContext } from "react-router-dom";
import { EditableAlert } from "../../rest/dto/alert.interfaces";
import { CreateAlertGuidedPage } from "../alerts-create-guided-page/alerts-create-guided-page.component";
import { AlertEditPageOutletContextProps } from "../alerts-update-page/alerts-update-page.interfaces";

export const CreateAlertNewUserPage: FunctionComponent = () => {
    const {
        alert,
        handleAlertPropertyChange,
        alertTemplateOptions,
        setShowBottomBar,
        handleSubmitAlertClick,
        refreshAlertTemplates,
    }: AlertEditPageOutletContextProps =
        useOutletContext<AlertEditPageOutletContextProps>();

    useEffect(() => {
        // If this page is under the configuration create alert
        // route, disable the parent's bottom bar
        setShowBottomBar(false);
    }, []);

    const handleOnSubmit = (
        alert: EditableAlert,
        suggestedName: string
    ): void => {
        const copied = { ...alert };

        if (alert.name === "") {
            copied.name = suggestedName;
        }

        handleSubmitAlertClick(copied);
    };

    return (
        <CreateAlertGuidedPage
            alert={alert}
            alertTemplates={alertTemplateOptions}
            getAlertTemplates={refreshAlertTemplates}
            isCreatingAlert={false}
            onAlertPropertyChange={handleAlertPropertyChange}
            onSubmit={handleOnSubmit}
        />
    );
};
