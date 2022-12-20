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
    useCallback,
    useEffect,
    useMemo,
    useState,
} from "react";
import { useTranslation } from "react-i18next";
import {
    Outlet,
    useLocation,
    useNavigate,
    useOutletContext,
} from "react-router-dom";
import { createNewStartingAlert } from "../../../components/alert-wizard-v2/alert-template/alert-template.utils";
import { generateAvailableAlgorithmOptions } from "../../../components/alert-wizard-v3/algorithm-selection/algorithm-selection.utils";
import { useAppBarConfigProvider } from "../../../components/app-bar/app-bar-config-provider/app-bar-config-provider.component";
import { PageHeader } from "../../../components/page-header/page-header.component";
import { LoadingErrorStateSwitch } from "../../../components/page-states/loading-error-state-switch/loading-error-state-switch.component";
import { generateEmptyEmailSendGridConfiguration } from "../../../components/subscription-group-wizard/groups-editor/groups-editor.utils";
import {
    NotificationTypeV1,
    PageContentsCardV1,
    PageContentsGridV1,
    PageV1,
    StepperV1,
    useNotificationProviderV1,
} from "../../../platform/components";
import { ActionStatus } from "../../../rest/actions.interfaces";
import { useGetAlertTemplates } from "../../../rest/alert-templates/alert-templates.actions";
import { EditableAlert } from "../../../rest/dto/alert.interfaces";
import { SubscriptionGroup } from "../../../rest/dto/subscription-group.interfaces";
import { useCreateSubscriptionGroup } from "../../../rest/subscription-groups/subscription-groups.actions";
import { generateGenericNameForAlert } from "../../../utils/alerts/alerts.util";
import {
    handleAlertPropertyChangeGenerator,
    handleCreateAlertClickGenerator,
} from "../../../utils/anomalies/anomalies.util";
import { QUERY_PARAM_KEYS } from "../../../utils/constants/constants.util";
import {
    AppRouteRelative,
    getHomePath,
} from "../../../utils/routes/routes.util";
import { createEmptySubscriptionGroup } from "../../../utils/subscription-groups/subscription-groups.util";
import { AlertEditPageOutletContextProps } from "../../alerts-update-page/alerts-update-page.interfaces";
import { CreateAlertPageProps } from "./create-alert-page.interface";

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

const MULTI_DIMENSION_SELECT_STEP = {
    subPath: AppRouteRelative.WELCOME_CREATE_ALERT_SETUP_DIMENSION_EXPLORATION,
    translationLabel: "multidimension-setup",
};

export const CreateAlertPage: FunctionComponent<CreateAlertPageProps> = ({
    hideHeader,
}) => {
    const { t } = useTranslation();
    const { pathname } = useLocation();
    const navigate = useNavigate();
    const { notify } = useNotificationProviderV1();
    const { setShowAppNavBar } = useAppBarConfigProvider();

    const configurationFlowParentContext: AlertEditPageOutletContextProps | null =
        useOutletContext<AlertEditPageOutletContextProps>();

    const {
        alertTemplates,
        getAlertTemplates,
        status: alertTemplatesRequestStatus,
    } = useGetAlertTemplates();

    const {
        createNewSubscriptionGroup,
        status: createSubscriptionGroupStatus,
    } = useCreateSubscriptionGroup();

    const [alert, setAlert] = useState<EditableAlert>(
        () => configurationFlowParentContext?.alert ?? createNewStartingAlert()
    );
    const [emails, setEmails] = useState<string[]>([]);
    const [createAlertStatus, setCreateAlertStatus] = useState<ActionStatus>(
        ActionStatus.Initial
    );

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

    const stepsToDisplay = useMemo(() => {
        const matchingDimensionExploration = [
            ...simpleOptions,
            ...advancedOptions,
        ].find(
            (c) =>
                c.algorithmOption.alertTemplateForMultidimension ===
                alert.template?.name
        );

        if (matchingDimensionExploration) {
            return [STEPS[0], MULTI_DIMENSION_SELECT_STEP, ...STEPS.slice(1)];
        }

        return [...STEPS];
    }, [alert]);

    const handleAlertPropertyChange = useMemo(() => {
        const localHandleAlertPropertyChange =
            handleAlertPropertyChangeGenerator(
                setAlert,
                [],
                () => {
                    return;
                },
                t
            );

        return (
            contentsToReplace: Partial<EditableAlert>,
            isTotalReplace = false
        ): void => {
            localHandleAlertPropertyChange(contentsToReplace, isTotalReplace);
            configurationFlowParentContext?.handleAlertPropertyChange(
                contentsToReplace,
                isTotalReplace
            );
        };
    }, [setAlert]);

    const activeStep = useMemo(() => {
        const activeStepDefinition = stepsToDisplay.find((candidate) =>
            pathname.includes(candidate.subPath)
        );

        if (!activeStepDefinition) {
            return "";
        }

        return activeStepDefinition.subPath;
    }, [pathname, stepsToDisplay]);

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

    const createAndHandleSubscriptionGroup = useMemo(() => {
        const queryParams = new URLSearchParams([
            [QUERY_PARAM_KEYS.SHOW_FIRST_ALERT_SUCCESS, "true"],
        ]);

        return handleCreateAlertClickGenerator(notify, t, () => {
            navigate(`${getHomePath()}?${queryParams.toString()}`);
            setShowAppNavBar(true);
        });
    }, [navigate, notify, t]);

    const handleCreateAlertClick = useCallback(() => {
        const alertWithName = { ...alert };

        if (!alertWithName.name) {
            alertWithName.name = generateGenericNameForAlert(
                alert.templateProperties.aggregationColumn as string,
                alert.templateProperties.aggregationFunction as string,
                selectedAlgorithmOption?.algorithmOption.title as string,
                selectedAlgorithmOption?.algorithmOption
                    .alertTemplateForMultidimension === alert?.template?.name
            );
        }

        if (configurationFlowParentContext?.handleSubmitAlertClick) {
            configurationFlowParentContext.handleSubmitAlertClick(
                alertWithName
            );

            return;
        }

        if (emails && emails.length > 0) {
            const subscriptionGroup = createEmptySubscriptionGroup();
            const newEmailConfiguration =
                generateEmptyEmailSendGridConfiguration();

            newEmailConfiguration.params.emailRecipients.to = emails;
            subscriptionGroup.specs = [newEmailConfiguration];
            subscriptionGroup.name = `${alertWithName.name}_subscription_group`;

            createNewSubscriptionGroup(subscriptionGroup)
                .then((savedSubscriptionGroup: SubscriptionGroup): void => {
                    createAndHandleSubscriptionGroup(
                        alertWithName,
                        [savedSubscriptionGroup],
                        setCreateAlertStatus
                    );
                })
                .catch((): void => {
                    createAndHandleSubscriptionGroup(
                        alertWithName,
                        [],
                        setCreateAlertStatus
                    );
                    notify(
                        NotificationTypeV1.Error,
                        t(
                            "message.skipped-creating-a-new-subscription-group-error"
                        )
                    );
                });
        } else {
            createAndHandleSubscriptionGroup(
                alertWithName,
                [],
                setCreateAlertStatus
            );
        }
    }, [alert, createAndHandleSubscriptionGroup, emails]);

    useEffect(() => {
        getAlertTemplates();

        // If this page is under the configuration create alert
        // route, disable the parent's bottom bar
        configurationFlowParentContext?.setShowBottomBar(false);
    }, []);

    return (
        <PageV1>
            {!hideHeader && (
                <PageHeader
                    transparentBackground
                    customActions={
                        <Button
                            color="primary"
                            href={t("url.documentation-homepage")}
                            target="_blank"
                            variant="contained"
                        >
                            {t("label.help")}
                        </Button>
                    }
                    subtitle={t("message.by-creating-an-alert-youll-be-able")}
                    title={t("message.lets-create-your-first-alert")}
                />
            )}

            <PageContentsGridV1>
                <Grid item xs={12}>
                    <PageContentsCardV1>
                        <Typography variant="h6">
                            {t("message.complete-the-following-steps")}
                        </Typography>
                        <StepperV1
                            activeStep={activeStep}
                            stepLabelFn={(step: string): string => {
                                const stepDefinition = stepsToDisplay.find(
                                    (candidate) => candidate.subPath === step
                                );

                                return t(
                                    `message.${stepDefinition?.translationLabel}`
                                );
                            }}
                            steps={stepsToDisplay.map((item) => item.subPath)}
                        />
                    </PageContentsCardV1>
                </Grid>
            </PageContentsGridV1>

            <LoadingErrorStateSwitch
                wrapInCard
                wrapInGrid
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
                        emails,
                        setEmails,
                        handleCreateAlertClick,
                        isCreatingAlert:
                            createSubscriptionGroupStatus ===
                                ActionStatus.Working ||
                            createAlertStatus === ActionStatus.Working,
                        getAlertTemplates,
                        alertTemplates,
                    }}
                />
            </LoadingErrorStateSwitch>
        </PageV1>
    );
};
