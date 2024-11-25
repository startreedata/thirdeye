/*
 * Copyright 2024 StarTree Inc
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
import React, { useEffect } from "react";
import { Link as RouterLink } from "react-router-dom";
import { getAlertsAlertViewPath } from "../../../utils/routes/routes.util";
import { Link, Typography } from "@material-ui/core";
import { ActiveIndicator } from "../../active-indicator/active-indicator.component";
import { useAlertDataStore } from "./store";
import { useInView } from "react-intersection-observer";
import { useGetAlert } from "../../../rest/alerts/alerts.actions";
import { Alert } from "../../../rest/dto/alert.interfaces";
import { ActionStatus } from "../../../rest/actions.interfaces";
import { SkeletonV1 } from "../../../platform/components";
import { useTranslation } from "react-i18next";

type AlertProp = {
    id: number;
};

export const RenderAlertName = ({ id }: AlertProp): JSX.Element => {
    const { t } = useTranslation();
    const { alertData, setAlertData } = useAlertDataStore();
    const { alert, getAlert, status } = useGetAlert();
    const { ref, inView } = useInView({
        triggerOnce: true,
        delay: 250,
        threshold: 1,
    });
    useEffect(() => {
        if (inView && id && !alertData[id] && id !== 150726) {
            getAlert(id);
        }
    }, [inView]);

    useEffect(() => {
        if (alert) {
            const alertObj: { [key: number]: Alert } = { [id]: alert };
            setAlertData({ ...alertData, ...alertObj });
        }
    }, [alert]);

    if (status === ActionStatus.Working) {
        return (
            <>
                <SkeletonV1 />
            </>
        );
    }
    if (status === ActionStatus.Error) {
        return (
            <Typography color="error" variant="body2">
                {t("message.experienced-issues-fetching-data-for-alert-id", {
                    alertId: id,
                })}
            </Typography>
        );
    }

    return (
        <Link
            component={RouterLink}
            innerRef={ref}
            to={getAlertsAlertViewPath(alertData[id]?.id)}
        >
            {alertData[id]?.name}
        </Link>
    );
};

export const RenderAlertStatus = ({ id }: AlertProp): JSX.Element => {
    const { alertData } = useAlertDataStore();

    return alertData[id] ? (
        <ActiveIndicator active={alertData[id]?.active === true} />
    ) : (
        <></>
    );
};
