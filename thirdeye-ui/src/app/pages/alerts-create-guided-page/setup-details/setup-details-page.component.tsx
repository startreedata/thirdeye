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
import { Box, Grid, Switch, TextField, Typography } from "@material-ui/core";
import {
    FunctionComponent,
    default as React,
    useEffect,
    useState,
} from "react";
import { useTranslation } from "react-i18next";
import {
    useNavigate,
    useOutletContext,
    useSearchParams,
} from "react-router-dom";
import { AlertFrequency } from "../../../components/alert-wizard-v2/alert-details/alert-frequency/alert-frequency.component";
import { NavigateAlertCreationFlowsDropdown } from "../../../components/alert-wizard-v3/navigate-alert-creation-flows-dropdown/navigate-alert-creation-flows-dropdown";
import { NotificationConfiguration } from "../../../components/alert-wizard-v3/notification-configuration/notification-configuration.component";
import { GranularityValue } from "../../../components/alert-wizard-v3/select-metric/select-metric.utils";
import { InputSection } from "../../../components/form-basics/input-section/input-section.component";
import { WizardBottomBar } from "../../../components/welcome-onboard-datasource/wizard-bottom-bar/wizard-bottom-bar.component";
import {
    PageContentsCardV1,
    PageContentsGridV1,
    useDialogProviderV1,
} from "../../../platform/components";
import { DialogType } from "../../../platform/components/dialog-provider-v1/dialog-provider-v1.interfaces";
import { generateGenericNameForAlert } from "../../../utils/alerts/alerts.util";
import { AppRouteRelative } from "../../../utils/routes/routes.util";
import { AlertCreatedGuidedPageOutletContext } from "../alerts-create-guided-page.interfaces";
import {
    SETUP_DETAILS_TEST_IDS,
    SetupDetailsPageProps,
} from "./setup-details-page.interface";

export const SetupDetailsPage: FunctionComponent<SetupDetailsPageProps> = ({
    inProgressLabel,
    createLabel,
}) => {
    const navigate = useNavigate();
    const { t } = useTranslation();
    const [searchParams] = useSearchParams();

    const {
        alert,
        onAlertPropertyChange,
        selectedAlgorithmOption,
        handleCreateAlertClick,
        isCreatingAlert,
        selectedSubscriptionGroups,
        handleSubscriptionGroupChange,
        newSubscriptionGroup,
        onNewSubscriptionGroupChange,
    } = useOutletContext<AlertCreatedGuidedPageOutletContext>();

    const { showDialog } = useDialogProviderV1();

    const [
        shouldDisplayScheduleConfiguration,
        setShouldDisplayScheduleConfiguration,
    ] = useState(false);
    const [isNotificationsOn, setIsNotificationsOn] = useState(
        selectedSubscriptionGroups.length > 0
    );

    useEffect(() => {
        // On initial render, ensure a metric, dataset, and dataSource are set
        if (
            !alert.templateProperties.aggregationColumn ||
            !alert.templateProperties.dataset ||
            !alert.templateProperties.dataSource
        ) {
            navigate(`../${AppRouteRelative.WELCOME_CREATE_ALERT_TUNE_ALERT}`);
        }
    }, []);

    const onCreateAlertClick: () => void = async () => {
        if (
            alert.templateProperties.monitoringGranularity &&
            (alert.templateProperties.monitoringGranularity ===
                GranularityValue.ONE_MINUTE ||
                alert.templateProperties.monitoringGranularity ===
                    GranularityValue.FIVE_MINUTES)
        ) {
            showDialog({
                type: DialogType.ALERT,
                contents: t(
                    "message.monitoring-granularity-below-15-minutes-warning"
                ),
                okButtonText: t("label.confirm"),
                cancelButtonText: t("label.cancel"),
                onOk: () => handleCreateAlertClick(alert),
            });
        } else {
            handleCreateAlertClick(alert);
        }
    };

    return (
        <>
            <PageContentsGridV1>
                <Grid item xs={12}>
                    <Grid
                        container
                        alignItems="center"
                        justifyContent="space-between"
                    >
                        <Grid item>
                            <Typography variant="h5">
                                {t("message.setup-alert-details")}
                            </Typography>
                            <Typography variant="body1">
                                {t(
                                    "message.add-the-final-details-for-your-alert"
                                )}
                            </Typography>
                        </Grid>
                        <Grid item>
                            <NavigateAlertCreationFlowsDropdown />
                        </Grid>
                    </Grid>
                </Grid>

                <Grid item xs={12}>
                    <PageContentsCardV1>
                        <Box marginBottom={2}>
                            <Typography variant="h5">
                                {t("label.alert-details")}
                            </Typography>
                        </Box>
                        <Grid container>
                            <InputSection
                                inputComponent={
                                    <TextField
                                        fullWidth
                                        data-testid={
                                            SETUP_DETAILS_TEST_IDS.NAME_INPUT
                                        }
                                        defaultValue={
                                            alert.name ||
                                            generateGenericNameForAlert(
                                                alert.templateProperties
                                                    .aggregationColumn as string,
                                                alert.templateProperties
                                                    .aggregationFunction as string,
                                                selectedAlgorithmOption
                                                    .algorithmOption
                                                    .title as string,
                                                selectedAlgorithmOption
                                                    ?.algorithmOption
                                                    .alertTemplateForMultidimension ===
                                                    alert?.template?.name
                                            )
                                        }
                                        onChange={(e) =>
                                            onAlertPropertyChange({
                                                name: e.currentTarget.value,
                                            })
                                        }
                                    />
                                }
                                label={t("label.name")}
                            />
                            <InputSection
                                helperLabel={`(${t("label.optional")})`}
                                inputComponent={
                                    <TextField
                                        fullWidth
                                        multiline
                                        data-testid={
                                            SETUP_DETAILS_TEST_IDS.DESCRIPTION_INPUT
                                        }
                                        defaultValue={alert.description}
                                        minRows={3}
                                        placeholder={t(
                                            "message.provide-an-optional-description-for-this-alert"
                                        )}
                                        onChange={(e) =>
                                            onAlertPropertyChange({
                                                description:
                                                    e.currentTarget.value,
                                            })
                                        }
                                    />
                                }
                                label={t("label.description")}
                            />
                        </Grid>
                    </PageContentsCardV1>
                </Grid>

                <Grid item xs={12}>
                    <PageContentsCardV1>
                        <Grid container>
                            <Grid item lg={3} md={5} sm={10} xs={10}>
                                <Box marginBottom={2}>
                                    <Typography variant="h5">
                                        {t("label.alert-schedule")}
                                    </Typography>
                                    <Typography variant="body2">
                                        {t("message.how-often-pipeline-checks")}
                                    </Typography>
                                </Box>
                            </Grid>
                            <Grid item lg={9} md={7} sm={2} xs={2}>
                                <Switch
                                    checked={shouldDisplayScheduleConfiguration}
                                    color="primary"
                                    name="checked"
                                    onChange={() =>
                                        setShouldDisplayScheduleConfiguration(
                                            !shouldDisplayScheduleConfiguration
                                        )
                                    }
                                />
                            </Grid>

                            {shouldDisplayScheduleConfiguration && (
                                <AlertFrequency
                                    alert={alert}
                                    onAlertPropertyChange={
                                        onAlertPropertyChange
                                    }
                                />
                            )}
                        </Grid>
                    </PageContentsCardV1>
                </Grid>

                <Grid item xs={12}>
                    <PageContentsCardV1>
                        <Grid container>
                            <Grid item lg={3} md={5} sm={10} xs={10}>
                                <Box marginBottom={2}>
                                    <Typography variant="h5">
                                        {t("label.configure-notifications")}
                                    </Typography>
                                    <Typography variant="body2">
                                        {t(
                                            "message.select-who-to-notify-when-finding-anomalies"
                                        )}
                                    </Typography>
                                </Box>
                            </Grid>
                            <Grid item lg={9} md={7} sm={2} xs={2}>
                                <Switch
                                    checked={isNotificationsOn}
                                    color="primary"
                                    data-testid={
                                        SETUP_DETAILS_TEST_IDS.CONFIGURATION_SWITCH
                                    }
                                    name="checked"
                                    onChange={() =>
                                        setIsNotificationsOn(!isNotificationsOn)
                                    }
                                />
                            </Grid>

                            {isNotificationsOn && (
                                <NotificationConfiguration
                                    alert={alert}
                                    initiallySelectedSubscriptionGroups={
                                        selectedSubscriptionGroups
                                    }
                                    newSubscriptionGroup={newSubscriptionGroup}
                                    onNewSubscriptionGroupChange={
                                        onNewSubscriptionGroupChange
                                    }
                                    onSubscriptionGroupsChange={
                                        handleSubscriptionGroupChange
                                    }
                                />
                            )}
                        </Grid>
                    </PageContentsCardV1>
                </Grid>
            </PageContentsGridV1>

            <WizardBottomBar
                backBtnLink={`../${
                    AppRouteRelative.WELCOME_CREATE_ALERT_ANOMALIES_FILTER
                }?${searchParams.toString()}`}
                handleNextClick={onCreateAlertClick}
                nextButtonIsDisabled={isCreatingAlert}
                nextButtonLabel={
                    isCreatingAlert ? inProgressLabel : createLabel
                }
            />
        </>
    );
};
