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
