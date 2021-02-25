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
import { getAlertsDetailPath } from "../../utils/routes/routes.util";
import {
    getSubscriptionGroupAlertId,
    getSubscriptionGroupAlertName,
    getSubscriptionGroupAlerts,
} from "../../utils/subscription-groups/subscription-groups.util";
import { SubscriptionGroupAlert } from "../entity-cards/subscription-group-card/subscription-group-card.interfaces";
import { TransferList } from "../transfer-list/transfer-list.component";
import { SubscriptionGroupAlertsAccordianProps } from "./subscription-group-alerts-accordian.interfaces";

export const SubscriptionGroupAlertsAccordian: FunctionComponent<SubscriptionGroupAlertsAccordianProps> = (
    props: SubscriptionGroupAlertsAccordianProps
) => {
    const history = useHistory();
    const { t } = useTranslation();

    const handleAlertClick = (
        subscriptionGroupAlert: SubscriptionGroupAlert
    ): void => {
        if (!subscriptionGroupAlert) {
            return;
        }

        history.push(getAlertsDetailPath(subscriptionGroupAlert.id));
    };

    const handleSubscriptionGroupAlertsChange = (
        subscriptionGroupAlerts: SubscriptionGroupAlert[]
    ): void => {
        props.onChange && props.onChange(subscriptionGroupAlerts);
    };

    return (
        <Accordion defaultExpanded={props.defaultExpanded} variant="outlined">
            {/* Header */}
            <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                <Typography variant="h6">{props.title}</Typography>
            </AccordionSummary>

            {/* Transfer list */}
            <AccordionDetails>
                <TransferList<SubscriptionGroupAlert>
                    link
                    fromLabel={t("label.all-entity", {
                        entity: t("label.alerts"),
                    })}
                    fromList={getSubscriptionGroupAlerts(props.alerts)}
                    listItemKeyFn={getSubscriptionGroupAlertId}
                    listItemTextFn={getSubscriptionGroupAlertName}
                    toLabel={t("label.subscribed-alerts")}
                    toList={
                        props.subscriptionGroupCardData &&
                        props.subscriptionGroupCardData.alerts
                    }
                    onChange={handleSubscriptionGroupAlertsChange}
                    onClick={handleAlertClick}
                />
            </AccordionDetails>
        </Accordion>
    );
};
