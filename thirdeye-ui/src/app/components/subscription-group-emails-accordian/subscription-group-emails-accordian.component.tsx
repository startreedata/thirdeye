import {
    Accordion,
    AccordionDetails,
    AccordionSummary,
    Typography,
} from "@material-ui/core";
import ExpandMoreIcon from "@material-ui/icons/ExpandMore";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { validateEmail } from "../../utils/validation/validation.util";
import { EditableList } from "../editable-list/editable-list.component";
import { SubscriptionGroupEmailsAccordianProps } from "./subscription-group-emails-accordian.interfaces";

export const SubscriptionGroupEmailsAccordian: FunctionComponent<
    SubscriptionGroupEmailsAccordianProps
> = (props: SubscriptionGroupEmailsAccordianProps) => {
    const { t } = useTranslation();

    return (
        <Accordion defaultExpanded={props.defaultExpanded} variant="outlined">
            {/* Header */}
            <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                <Typography variant="h6">{props.title}</Typography>
            </AccordionSummary>

            {/* Subscription group emails editable list */}
            <AccordionDetails>
                <EditableList
                    addButtonLabel={t("label.add")}
                    inputLabel={t("label.add-entity", {
                        entity: t("label.email"),
                    })}
                    list={
                        (props.subscriptionGroup &&
                            props.subscriptionGroup.emails) ||
                        []
                    }
                    loading={!props.subscriptionGroup}
                    validateFn={validateEmail}
                    onChange={props.onChange}
                />
            </AccordionDetails>
        </Accordion>
    );
};
