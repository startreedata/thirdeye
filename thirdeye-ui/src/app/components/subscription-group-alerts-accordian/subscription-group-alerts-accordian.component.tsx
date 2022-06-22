/**
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import {
    Accordion,
    AccordionDetails,
    AccordionSummary,
    Typography,
} from "@material-ui/core";
import ExpandMoreIcon from "@material-ui/icons/ExpandMore";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";
import { Alert } from "../../rest/dto/alert.interfaces";
import { getAlertsViewPath } from "../../utils/routes/routes.util";
import {
    getUiSubscriptionGroupAlertId,
    getUiSubscriptionGroupAlertName,
} from "../../utils/subscription-groups/subscription-groups.util";
import { TransferList } from "../transfer-list/transfer-list.component";
import { SubscriptionGroupAlertsAccordianProps } from "./subscription-group-alerts-accordian.interfaces";

export const SubscriptionGroupAlertsAccordian: FunctionComponent<
    SubscriptionGroupAlertsAccordianProps
> = (props: SubscriptionGroupAlertsAccordianProps) => {
    const navigate = useNavigate();
    const { t } = useTranslation();

    const handleAlertClick = (alert: Alert): void => {
        navigate(getAlertsViewPath(alert.id));
    };

    return (
        <Accordion defaultExpanded={props.defaultExpanded} variant="outlined">
            {/* Header */}
            <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                <Typography variant="h6">{props.title}</Typography>
            </AccordionSummary>

            {/* Subscription group alerts transfer list */}
            <AccordionDetails>
                <TransferList
                    link
                    fromLabel={t("label.all-entity", {
                        entity: t("label.alerts"),
                    })}
                    fromList={props.alerts}
                    listItemKeyFn={getUiSubscriptionGroupAlertId}
                    listItemTextFn={getUiSubscriptionGroupAlertName}
                    loading={!props.subscriptionGroup}
                    toLabel={t("label.subscribed-alerts")}
                    toList={
                        (props.subscriptionGroup &&
                            (props.subscriptionGroup.alerts as Alert[])) ||
                        []
                    }
                    onChange={props.onChange}
                    onClick={handleAlertClick}
                />
            </AccordionDetails>
        </Accordion>
    );
};
