import {
    Accordion,
    AccordionDetails,
    AccordionSummary,
    Typography,
} from "@material-ui/core";
import ExpandMoreIcon from "@material-ui/icons/ExpandMore";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { useHistory } from "react-router-dom";
import { UiSubscriptionGroupAlert } from "../../rest/dto/ui-subscription-group.interfaces";
import { getAlertsDetailPath } from "../../utils/routes/routes.util";
import {
    getUiSubscriptionGroupAlertId,
    getUiSubscriptionGroupAlertName,
    getUiSubscriptionGroupAlerts,
} from "../../utils/subscription-groups/subscription-groups.util";
import { TransferList } from "../transfer-list/transfer-list.component";
import { UiSubscriptionGroupAlertsAccordianProps } from "./subscription-group-alerts-accordian.interfaces";

export const UiSubscriptionGroupAlertsAccordian: FunctionComponent<UiSubscriptionGroupAlertsAccordianProps> = (
    props: UiSubscriptionGroupAlertsAccordianProps
) => {
    const history = useHistory();
    const { t } = useTranslation();

    const handleAlertClick = (
        uiSubscriptionGroupAlert: UiSubscriptionGroupAlert
    ): void => {
        if (!uiSubscriptionGroupAlert) {
            return;
        }

        history.push(getAlertsDetailPath(uiSubscriptionGroupAlert.id));
    };

    const handleUiSubscriptionGroupAlertsChange = (
        uiSubscriptionGroupAlerts: UiSubscriptionGroupAlert[]
    ): void => {
        props.onChange && props.onChange(uiSubscriptionGroupAlerts);
    };

    return (
        <Accordion defaultExpanded={props.defaultExpanded} variant="outlined">
            {/* Header */}
            <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                <Typography variant="h6">{props.title}</Typography>
            </AccordionSummary>

            {/* Transfer list */}
            <AccordionDetails>
                <TransferList<UiSubscriptionGroupAlert>
                    link
                    fromLabel={t("label.all-entity", {
                        entity: t("label.alerts"),
                    })}
                    fromList={getUiSubscriptionGroupAlerts(props.alerts)}
                    listItemKeyFn={getUiSubscriptionGroupAlertId}
                    listItemTextFn={getUiSubscriptionGroupAlertName}
                    toLabel={t("label.subscribed-alerts")}
                    toList={
                        props.uiSubscriptionGroup &&
                        props.uiSubscriptionGroup.alerts
                    }
                    onChange={handleUiSubscriptionGroupAlertsChange}
                    onClick={handleAlertClick}
                />
            </AccordionDetails>
        </Accordion>
    );
};
