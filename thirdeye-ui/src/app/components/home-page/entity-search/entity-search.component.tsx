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
import { Typography } from "@material-ui/core";
import TextField from "@material-ui/core/TextField";
import { Autocomplete } from "@material-ui/lab";
import React, { FunctionComponent, useMemo } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";
import {
    getAlertsAlertViewPath,
    getAnomaliesViewPath,
    getSubscriptionGroupsViewPath,
} from "../../../utils/routes/routes.util";
import { EntityOption } from "../active-alerts-count/active-alerts-count.interfaces";
import { EntitySearchProps } from "./entity-search.interfaces";
import { useEntitySearchStyles } from "./entity-search.styles";

export const EntitySearch: FunctionComponent<EntitySearchProps> = ({
    subscriptionGroups,
    alerts,
    anomalies,
}) => {
    const classes = useEntitySearchStyles();
    const navigate = useNavigate();
    const { t } = useTranslation();
    const availableOptions: EntityOption[] = useMemo(() => {
        const options: EntityOption[] = [];

        if (alerts) {
            alerts.forEach((alert) => {
                options.push({
                    type: t("label.alert"),
                    label: alert.name,
                    link: getAlertsAlertViewPath(alert.id),
                });
            });
        }

        if (subscriptionGroups) {
            subscriptionGroups.forEach((subscriptionGroup) => {
                options.push({
                    type: t("label.subscription-group"),
                    label: subscriptionGroup.name,
                    link: getSubscriptionGroupsViewPath(subscriptionGroup.id),
                });
            });
        }

        if (anomalies) {
            anomalies.forEach((anomaly) => {
                options.push({
                    type: t("label.anomaly"),
                    label: `${anomaly.alert.name} : ${anomaly.id}`,
                    link: getAnomaliesViewPath(anomaly.id),
                });
            });
        }

        return options;
    }, [subscriptionGroups, alerts, anomalies]);

    const handleSomething = (option: EntityOption | null): void => {
        if (option) {
            navigate(option.link);
        }
    };

    return (
        <Autocomplete<EntityOption>
            fullWidth
            getOptionLabel={(option) => option.label}
            options={availableOptions}
            renderInput={(params) => (
                <TextField
                    {...params}
                    className={classes.entitySearchInput}
                    placeholder="Search alerts or subscription groups"
                    variant="outlined"
                />
            )}
            renderOption={(option) => {
                return (
                    <div>
                        <Typography variant="h6">{option.label}</Typography>
                        <Typography variant="caption">{option.type}</Typography>
                    </div>
                );
            }}
            onChange={(_, selectedValue) => {
                handleSomething(selectedValue);
            }}
        />
    );
};
