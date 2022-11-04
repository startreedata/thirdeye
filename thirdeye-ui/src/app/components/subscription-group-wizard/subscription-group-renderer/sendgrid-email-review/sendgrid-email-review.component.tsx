import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { SendgridEmailReviewProps } from "./sendgrid-email-review.interfaces";

export const SendgridEmailReview: FunctionComponent<SendgridEmailReviewProps> =
    ({ configuration }) => {
        const { t } = useTranslation();

        return (
            <div>
                <div>
                    <strong>{t("label.to")}: </strong>
                    {configuration.params.emailRecipients.to.join(", ")}
                </div>
                <div>
                    <strong>{t("label.from")}: </strong>
                    {configuration.params.emailRecipients.from}
                </div>
                <div>
                    <strong>{t("label.sendgrid-api-key")}: </strong>
                    {configuration.params.apiKey}
                </div>
            </div>
        );
    };
