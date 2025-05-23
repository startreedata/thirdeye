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

import {
    Box,
    FormControl,
    FormControlLabel,
    FormLabel,
    Radio,
    RadioGroup,
    Typography,
} from "@material-ui/core";
import { AxiosError } from "axios";
import { capitalize, isEmpty } from "lodash";
import React, {
    FunctionComponent,
    useCallback,
    useEffect,
    useMemo,
    useState,
} from "react";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";
import { LoadingErrorStateSwitch } from "../../../../components/page-states/loading-error-state-switch/loading-error-state-switch.component";
import { WizardBottomBar } from "../../../../components/welcome-onboard-datasource/wizard-bottom-bar/wizard-bottom-bar.component";
import {
    JSONEditorV1,
    NotificationTypeV1,
    PageContentsCardV1,
    useNotificationProviderV1,
} from "../../../../platform/components";
import { ActionStatus } from "../../../../rest/actions.interfaces";
import {
    useGetDatasources,
    useGetRecommendedDatasources,
} from "../../../../rest/datasources/datasources.actions";
import { createDatasource } from "../../../../rest/datasources/datasources.rest";
import type { Datasource } from "../../../../rest/dto/datasource.interfaces";
import { createDefaultDatasource } from "../../../../utils/datasources/datasources.util";
import { notifyIfErrors } from "../../../../utils/notifications/notifications.util";
import { getErrorMessages } from "../../../../utils/rest/rest.util";
import {
    AppRoute,
    getDataConfigurationCreateDatasetsPath,
} from "../../../../utils/routes/routes.util";
import type {
    DatasourceOptionGroups,
    SelectedDatasource,
} from "./onboard-datasource-page.interfaces";
import {
    ADD_NEW_DATASOURCE,
    getDatasourceGroups,
} from "./onboard-datasource-page.utils";

export const WelcomeSelectDatasource: FunctionComponent = () => {
    const navigate = useNavigate();
    const [editedDatasource, setEditedDatasource] = useState<Datasource>(
        createDefaultDatasource()
    );
    const [selectedDatasourceValue, setSelectedDatasourceValue] =
        useState<SelectedDatasource>(null);

    const { notify } = useNotificationProviderV1();
    const { t } = useTranslation();

    const handleDatasourceChange = (value: string): void => {
        setEditedDatasource(JSON.parse(value));
    };

    const handleRadioChange = (
        _e: React.ChangeEvent<HTMLInputElement>,
        value: string
    ): void => {
        setSelectedDatasourceValue(value as SelectedDatasource);
    };

    const {
        datasources,
        getDatasources,
        status: datasourcesStatus,
        errorMessages: datasourcesErrormessage,
    } = useGetDatasources();

    const {
        recommendedDatasource,
        getRecommendedDatasource,
        status: recommendedDatasourcesStatus,
        errorMessages: recommendedDatasourcesErrormessage,
    } = useGetRecommendedDatasources();

    const datasourceGroups = useMemo<DatasourceOptionGroups[]>(
        () => getDatasourceGroups(datasources || [], t),
        [datasources]
    );

    useEffect(() => {
        getDatasources();
    }, []);

    useEffect(() => {
        if (datasourcesStatus === ActionStatus.Done && isEmpty(datasources)) {
            getRecommendedDatasource();
        }
    }, [datasourcesStatus, datasources]);

    useEffect(() => {
        if (!isEmpty(recommendedDatasource) && isEmpty(datasources)) {
            createDatasource(recommendedDatasource!)
                .then(() => {
                    getDatasources();
                })
                .catch((error: AxiosError): void => {
                    notifyIfErrors(
                        ActionStatus.Error,
                        getErrorMessages(error),
                        notify,
                        t("message.create-error", {
                            entity: t("label.datasource"),
                        })
                    );
                });
        }
    }, [recommendedDatasource, datasources]);

    useEffect(() => {
        if (
            datasourcesStatus === ActionStatus.Done &&
            recommendedDatasourcesStatus === ActionStatus.Done
        ) {
            if (isEmpty(datasources) && isEmpty(recommendedDatasource)) {
                notify(
                    NotificationTypeV1.Info,
                    t("info.pinot-instance-not-detected")
                );
                setSelectedDatasourceValue(ADD_NEW_DATASOURCE);
            }
        }
    }, [
        datasourcesStatus,
        recommendedDatasource,
        datasources,
        recommendedDatasource,
    ]);

    useEffect(() => {
        notifyIfErrors(
            datasourcesStatus,
            datasourcesErrormessage,
            notify,
            t("message.error-while-fetching", {
                entity: t("label.datasources"),
            })
        );
    }, [datasourcesStatus]);

    useEffect(() => {
        notifyIfErrors(
            recommendedDatasourcesStatus,
            recommendedDatasourcesErrormessage,
            notify,
            t("errors.pinot-instance-not-created")
        );
        if (recommendedDatasourcesStatus === ActionStatus.Error) {
            setSelectedDatasourceValue(ADD_NEW_DATASOURCE);
        }
    }, [recommendedDatasourcesStatus]);

    const goToDatasetPage = useCallback(
        (datasourceId: number) =>
            navigate(getDataConfigurationCreateDatasetsPath(datasourceId)),
        []
    );

    const handleCreateNewDatasource = useCallback(
        (editedDatasourceProp: Datasource) =>
            createDatasource(editedDatasourceProp)
                .then((datasource: Datasource): number => {
                    notify(
                        NotificationTypeV1.Success,
                        t("message.create-success", {
                            entity: t("label.datasource"),
                        })
                    );

                    setSelectedDatasourceValue(datasource.id);

                    return datasource.id;
                })
                .catch((error: AxiosError): void => {
                    notifyIfErrors(
                        ActionStatus.Error,
                        getErrorMessages(error),
                        notify,
                        t("message.create-error", {
                            entity: t("label.datasource"),
                        })
                    );
                }),
        []
    );

    const handleNext = useCallback((): void | Promise<void> => {
        if (selectedDatasourceValue === null) {
            notify(
                NotificationTypeV1.Error,
                "Please select a valid dataset or create a new one"
            );

            return;
        }

        if (selectedDatasourceValue === ADD_NEW_DATASOURCE) {
            return handleCreateNewDatasource(editedDatasource).then(
                (created) => {
                    if (!created) {
                        return;
                    }

                    goToDatasetPage(created);
                }
            );
        }

        goToDatasetPage(selectedDatasourceValue);
    }, [editedDatasource, selectedDatasourceValue]);

    return (
        <>
            <Box alignContent="center" display="flex" flexDirection="column">
                <PageContentsCardV1>
                    <Typography
                        align="center"
                        style={{ marginLeft: "-20px" }}
                        variant="h5"
                    >
                        Select your data source
                    </Typography>
                    <Typography align="center" variant="body2">
                        {capitalize(t("message.te-will-monitor-your-data"))}
                    </Typography>

                    <LoadingErrorStateSwitch
                        wrapInCard
                        wrapInGrid
                        isError={datasourcesStatus === ActionStatus.Error}
                        isLoading={
                            datasourcesStatus === ActionStatus.Working ||
                            datasourcesStatus === ActionStatus.Initial ||
                            recommendedDatasourcesStatus ===
                                ActionStatus.Working
                        }
                    >
                        {datasourceGroups.map((datasourceGroup) => (
                            <>
                                {datasourceGroup.options.length > 0 && (
                                    <Box
                                        display="flex"
                                        justifyContent="center"
                                        key={datasourceGroup.key}
                                        marginTop="20px"
                                        px={2}
                                        py={1}
                                    >
                                        <Box
                                            display="flex"
                                            justifyContent="start"
                                            marginLeft="50px"
                                            minWidth="250px"
                                            width="250px"
                                        >
                                            <FormControl component="fieldset">
                                                <FormLabel
                                                    color="secondary"
                                                    component="legend"
                                                >
                                                    {datasourceGroup.title}
                                                </FormLabel>
                                                <RadioGroup
                                                    aria-label={t(
                                                        "message.select-entity",
                                                        {
                                                            entity: t(
                                                                "label.datasource"
                                                            ),
                                                        }
                                                    )}
                                                    name="select-datasource"
                                                    value={
                                                        selectedDatasourceValue
                                                    }
                                                    onChange={handleRadioChange}
                                                >
                                                    {datasourceGroup.options.map(
                                                        (datasourceOption) => (
                                                            <FormControlLabel
                                                                control={
                                                                    <Radio />
                                                                }
                                                                key={
                                                                    datasourceOption.value
                                                                }
                                                                label={
                                                                    datasourceOption.label
                                                                }
                                                                value={datasourceOption.value.toString()}
                                                            />
                                                        )
                                                    )}
                                                </RadioGroup>
                                            </FormControl>
                                        </Box>
                                    </Box>
                                )}
                            </>
                        ))}
                    </LoadingErrorStateSwitch>

                    {selectedDatasourceValue === ADD_NEW_DATASOURCE ? (
                        <JSONEditorV1<Datasource>
                            hideValidationSuccessIcon
                            value={editedDatasource}
                            onChange={handleDatasourceChange}
                        />
                    ) : null}
                </PageContentsCardV1>
            </Box>
            <WizardBottomBar
                backBtnLink={AppRoute.WELCOME}
                handleNextClick={handleNext}
                nextButtonLabel={t("label.next")}
            />
        </>
    );
};
