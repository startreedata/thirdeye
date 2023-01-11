/*
 * Copyright 2022 StarTree Inc
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
import React, { FunctionComponent, useState } from "react";
import { useTranslation } from "react-i18next";
import { SpecType } from "../../../../rest/dto/subscription-group.interfaces";
import { EmailListInput } from "../../../form-basics/email-list-input/email-list-input.component";
import { InputSection } from "../../../form-basics/input-section/input-section.component";
import { generateEmptyEmailSendGridConfiguration } from "../../../subscription-group-wizard/groups-editor/groups-editor.utils";
import { OnlyEmailsProps } from "./only-emails.interfaces";

export const OnlyEmails: FunctionComponent<OnlyEmailsProps> = ({
    subscriptionGroup,
    onSubscriptionGroupChange,
}) => {
    const { t } = useTranslation();

    const [emails, setEmails] = useState<string[]>(() => {
        if (
            subscriptionGroup &&
            subscriptionGroup.specs.length === 1 &&
            subscriptionGroup.specs[0].type === SpecType.EmailSendgrid &&
            subscriptionGroup.specs[0].params.emailRecipients.to &&
            subscriptionGroup.specs[0].params.emailRecipients.to.length > 0
        ) {
            return subscriptionGroup.specs[0].params.emailRecipients.to;
        }

        return [];
    });

    const handleEmailsChange = (emails: string[]): void => {
        setEmails(emails);

        const newEmailConfiguration = generateEmptyEmailSendGridConfiguration();
        newEmailConfiguration.params.emailRecipients.to = emails;
        subscriptionGroup.specs = [newEmailConfiguration];
        onSubscriptionGroupChange({ ...subscriptionGroup });
    };

    return (
        <InputSection
            inputComponent={
                <EmailListInput emails={emails} onChange={handleEmailsChange} />
            }
            label={t("label.add-email")}
        />
    );
};
