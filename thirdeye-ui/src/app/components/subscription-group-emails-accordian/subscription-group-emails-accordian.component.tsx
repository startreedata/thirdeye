import {
    Accordion,
    AccordionDetails,
    AccordionSummary,
    Typography,
} from "@material-ui/core";
import { ExpandMore } from "@material-ui/icons";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { validateEmail } from "../../utils/validation-util/validation-util";
import { EditableList } from "../editable-list/editable-list.component";
import { SubscriptionGroupEmailsAccordianProps } from "./subscription-group-emails-accordian.interfaces";

export const SubscriptionGroupEmailsAccordian: FunctionComponent<SubscriptionGroupEmailsAccordianProps> = (
    props: SubscriptionGroupEmailsAccordianProps
) => {
    const { t } = useTranslation();

    const onSubscriptionGroupEmailsChange = (emails: string[]): void => {
        props.onChange && props.onChange(emails);
    };

    return (
        <Accordion defaultExpanded={props.defaultExpanded} variant="outlined">
            {/* Header */}
            <AccordionSummary expandIcon={<ExpandMore />}>
                <Typography variant="h6">{props.title}</Typography>
            </AccordionSummary>

            {/* Subscription group emails editable list */}
            <AccordionDetails>
                <EditableList
                    addButtonLabel={t("label.add")}
                    inputLabel={t("label.add-email")}
                    list={
                        props.subscriptionGroupCardData &&
                        props.subscriptionGroupCardData.emails
                    }
                    validateFn={validateEmail}
                    onChange={onSubscriptionGroupEmailsChange}
                />
            </AccordionDetails>
        </Accordion>
    );
};
