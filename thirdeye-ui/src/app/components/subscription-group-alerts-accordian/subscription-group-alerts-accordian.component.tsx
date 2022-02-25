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
