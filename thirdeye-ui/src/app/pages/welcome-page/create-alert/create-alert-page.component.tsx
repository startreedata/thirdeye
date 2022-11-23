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
import { Button, Grid, Typography } from "@material-ui/core";
import {
    default as React,
    FunctionComponent,
    useEffect,
    useMemo,
    useState,
} from "react";
import { useTranslation } from "react-i18next";
import { Outlet, useLocation } from "react-router-dom";
import { createNewStartingAlert } from "../../../components/alert-wizard-v2/alert-template/alert-template.utils";
import { generateAvailableAlgorithmOptions } from "../../../components/alert-wizard-v3/algorithm-selection/algorithm-selection.utils";
import { PageHeader } from "../../../components/page-header/page-header.component";
import { LoadingErrorStateSwitch } from "../../../components/page-states/loading-error-state-switch/loading-error-state-switch.component";
import {
    PageContentsCardV1,
    PageContentsGridV1,
    PageV1,
    StepperV1,
} from "../../../platform/components";
import { ActionStatus } from "../../../rest/actions.interfaces";
import { useGetAlertTemplates } from "../../../rest/alert-templates/alert-templates.actions";
import { EditableAlert } from "../../../rest/dto/alert.interfaces";
import { handleAlertPropertyChangeGenerator } from "../../../utils/anomalies/anomalies.util";
import { AppRouteRelative } from "../../../utils/routes/routes.util";

const STEPS = [
    {
        subPath: AppRouteRelative.WELCOME_CREATE_ALERT_SELECT_TYPE,
        translationLabel: "select-alert-type",
    },
    {
        subPath: AppRouteRelative.WELCOME_CREATE_ALERT_SETUP_MONITORING,
        translationLabel: "setup-alert-monitoring",
    },
    {
        subPath: AppRouteRelative.WELCOME_CREATE_ALERT_SETUP_DETAILS,
        translationLabel: "setup-alert-details",
    },
];

export const CreateAlertPage: FunctionComponent = () => {
    const { t } = useTranslation();
    const { pathname } = useLocation();

    const {
        alertTemplates,
        getAlertTemplates,
        status: alertTemplatesRequestStatus,
    } = useGetAlertTemplates();

    // Ensure to filter for what is available on the server
    const [simpleOptions, advancedOptions] = useMemo(() => {
        if (!alertTemplates) {
            return [[], []];
        }
        const availableTemplateNames = alertTemplates.map(
            (alertTemplate) => alertTemplate.name
        );

        return generateAvailableAlgorithmOptions(t, availableTemplateNames);
    }, [alertTemplates]);

    const [alert, setAlert] = useState<EditableAlert>(() =>
        createNewStartingAlert()
    );

    const handleAlertPropertyChange = useMemo(() => {
        return handleAlertPropertyChangeGenerator(
            setAlert,
            [],
            () => {
                return;
            },
            t
        );
    }, [setAlert]);

    const activeStep = useMemo(() => {
        const activeStepDefinition = STEPS.find((candidate) =>
            pathname.includes(candidate.subPath)
        );

        if (!activeStepDefinition) {
            return "";
        }

        return activeStepDefinition.subPath;
    }, [pathname]);

    const selectedAlgorithmOption = useMemo(() => {
        return [...simpleOptions, ...advancedOptions].find(
            (c) =>
                c.algorithmOption.alertTemplate === alert.template?.name ||
                c.algorithmOption.alertTemplateForPercentile ===
                    alert.template?.name ||
                c.algorithmOption.alertTemplateForMultidimension ===
                    alert.template?.name
        );
    }, [alert, simpleOptions, advancedOptions]);

    useEffect(() => {
        getAlertTemplates();
    }, []);

    useEffect(() => {
        console.log(alert);
    }, [alert]);

    return (
        <PageV1>
            <PageHeader
                transparentBackground
                customActions={<Button>{t("label.help")}</Button>}
                subtitle={t("message.by-creating-an-alert-youll-be-able")}
                title={t("message.lets-create-your-first-alert")}
            />
            <PageContentsGridV1>
                <Grid item xs={12}>
                    <PageContentsCardV1>
                        <Typography variant="h6">
                            {t("message.complete-the-following-steps")}
                        </Typography>
                        <StepperV1
                            activeStep={activeStep}
                            stepLabelFn={(step: string): string => {
                                const stepDefinition = STEPS.find(
                                    (candidate) => candidate.subPath === step
                                );

                                return t(
                                    `message.${stepDefinition?.translationLabel}`
                                );
                            }}
                            steps={STEPS.map((item) => item.subPath)}
                        />
                    </PageContentsCardV1>
                </Grid>
            </PageContentsGridV1>

            <LoadingErrorStateSwitch
                isError={false}
                isLoading={alertTemplatesRequestStatus === ActionStatus.Working}
            >
                <Outlet
                    context={{
                        alert,
                        handleAlertPropertyChange,
                        simpleOptions,
                        advancedOptions,
                        selectedAlgorithmOption,
                    }}
                />
            </LoadingErrorStateSwitch>
        </PageV1>
    );
};
