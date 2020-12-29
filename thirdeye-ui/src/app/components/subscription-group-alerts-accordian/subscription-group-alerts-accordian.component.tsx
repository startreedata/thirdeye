import {
    Accordion,
    AccordionDetails,
    AccordionSummary,
    Typography,
} from "@material-ui/core";
import { ExpandMore } from "@material-ui/icons";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import {
    getSubscriptionGroupAlertId,
    getSubscriptionGroupAlertName,
} from "../../utils/subscription-group-util/subscription-group-util";
import { SubscriptionGroupAlert } from "../subscription-group-card/subscription-group-card.interfaces";
import { TransferList } from "../transfer-list/transfer-list.component";
import { SubscriptionGroupAlertsAccordianProps } from "./subscription-group-alerts-accordian.interfaces";

export const SubscriptionGroupAlertsAccordian: FunctionComponent<SubscriptionGroupAlertsAccordianProps> = (
    props: SubscriptionGroupAlertsAccordianProps
) => {
    const { t } = useTranslation();

    const onSubscriptionGroupAlertsChange = (
        subscriptiongroupAlerts: SubscriptionGroupAlert[]
    ): void => {
        props.onChange && props.onChange(subscriptiongroupAlerts);
    };

    return (
        <Accordion defaultExpanded={props.defaultExpanded} variant="outlined">
            {/* Header */}
            <AccordionSummary expandIcon={<ExpandMore />}>
                <Typography variant="h6">{props.title}</Typography>
            </AccordionSummary>

            {/* Subscription group alerts transfer list */}
            <AccordionDetails>
                <TransferList<SubscriptionGroupAlert>
                    fromLabel={t("label.all-alerts")}
                    fromList={props.alerts}
                    listItemKeyFn={getSubscriptionGroupAlertId}
                    listItemTextFn={getSubscriptionGroupAlertName}
                    toLabel={t("label.subscribed-alerts")}
                    toList={props.subscriptionGroup.alerts}
                    onChange={onSubscriptionGroupAlertsChange}
                />
            </AccordionDetails>
        </Accordion>
    );
};
