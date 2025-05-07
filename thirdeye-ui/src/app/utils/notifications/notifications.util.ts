/*
 * Copyright 2023 StarTree Inc
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
import { isEmpty } from "lodash";
import { SlackFormEntries } from "../../components/subscription-group-wizard/subscription-group-details/recipient-details/groups-editor/slack/slack.interfaces";
import { NotificationTypeV1 } from "../../platform/components";
import { ErrorMessage } from "../../platform/components/notification-provider-v1/notification-provider-v1/notification-provider-v1.interfaces";
import { ActionStatus } from "../../rest/actions.interfaces";
import { SlackSpec } from "../../rest/dto/subscription-group.interfaces";

export const notifyIfErrors = (
    requestStatus: ActionStatus,
    errorMessages: ErrorMessage[] | null | undefined,
    notify: (
        msgType: NotificationTypeV1,
        msg: string,
        details?: string
    ) => void,
    fallbackMsg: string
): void => {
    if (requestStatus !== ActionStatus.Error) {
        return;
    }

    if (!isEmpty(errorMessages)) {
        errorMessages
            ?.reverse() // First in first shown
            .map((msg) =>
                notify(NotificationTypeV1.Error, msg.message, msg.details)
            );
    } else {
        notify(NotificationTypeV1.Error, fallbackMsg);
    }
};

export const convertSlackConfigurationToSlackFormEntries = (
    slackConfig: SlackSpec["params"]
): SlackFormEntries => {
    return {
        webhookUrl: slackConfig.webhookUrl,
        notifyResolvedAnomalies: slackConfig.notifyResolvedAnomalies,
        sendOneMessagePerAnomaly: slackConfig.sendOneMessagePerAnomaly,
        textConfiguration: {
            owner: slackConfig.textConfiguration?.owner ?? "",
            mentionMemberIds:
                slackConfig.textConfiguration?.mentionMemberIds?.map((id) => ({
                    value: id,
                })) ?? [],
        },
    };
};

export const validateSlackMemberIDFormat = (memberId: string): boolean => {
    return /^[US][0-9A-Z]{8,}$/.test(memberId);
};
