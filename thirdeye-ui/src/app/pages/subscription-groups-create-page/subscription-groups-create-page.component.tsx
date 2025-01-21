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
import { AxiosError } from "axios";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";
import {
    NotificationTypeV1,
    useDialogProviderV1,
    useNotificationProviderV1,
} from "../../platform/components";
import { ActionStatus } from "../../rest/actions.interfaces";
import { SubscriptionGroup } from "../../rest/dto/subscription-group.interfaces";
import { createSubscriptionGroup } from "../../rest/subscription-groups/subscription-groups.rest";
import { notifyIfErrors } from "../../utils/notifications/notifications.util";
import { getErrorMessages } from "../../utils/rest/rest.util";
import {
    getSubscriptionGroupsAllPath,
    getSubscriptionGroupsCreatePath,
    getSubscriptionGroupsViewPath,
} from "../../utils/routes/routes.util";
import { SubscriptionGroupsWizardPage } from "../subscription-groups-wizard-page/subscription-groups-wizard-page.component";
import { useAppBarConfigProvider } from "../../components/app-bar/app-bar-config-provider/app-bar-config-provider.component";
import { DialogType } from "../../platform/components/dialog-provider-v1/dialog-provider-v1.interfaces";

export const SubscriptionGroupsCreatePage: FunctionComponent = () => {
    const navigate = useNavigate();
    const { t } = useTranslation();
    const { notify } = useNotificationProviderV1();
    const { remainingQuota } = useAppBarConfigProvider();
    const { showDialog } = useDialogProviderV1();

    const checkQuota = (subscriptionGroup: SubscriptionGroup): void => {
        if (remainingQuota?.notification && remainingQuota.notification <= 0) {
            showDialog({
                type: DialogType.ALERT,
                contents: (
                    <>
                        <div>{t("message.quota-expired-notification")}</div>
                        <div>
                            {t("message.contact-support", {
                                message:
                                    "For questions or to request a higher quota,",
                            })}
                        </div>
                    </>
                ),
                okButtonText: t("label.confirm"),
                cancelButtonText: t("label.cancel"),
                onOk: () =>
                    handleSubscriptionGroupWizardFinish(subscriptionGroup),
            });
        } else {
            handleSubscriptionGroupWizardFinish(subscriptionGroup);
        }
    };

    const handleSubscriptionGroupWizardFinish = (
        subscriptionGroup: SubscriptionGroup
    ): void => {
        if (!subscriptionGroup) {
            return;
        }

        createSubscriptionGroup(subscriptionGroup)
            .then((subscriptionGroup: SubscriptionGroup): void => {
                notify(
                    NotificationTypeV1.Success,
                    t("message.create-success", {
                        entity: t("label.subscription-group"),
                    })
                );

                // Redirect to subscription groups detail path
                navigate(getSubscriptionGroupsViewPath(subscriptionGroup.id));
            })
            .catch((error: AxiosError): void => {
                notifyIfErrors(
                    ActionStatus.Error,
                    getErrorMessages(error),
                    notify,
                    t("message.create-error", {
                        entity: t("label.subscription-group"),
                    })
                );
            });
    };

    const handleOnCancelClick = (): void => {
        navigate(getSubscriptionGroupsAllPath());
    };

    const pageHeaderTitle = t(`label.create-entity`, {
        entity: t("label.subscription-group"),
    });

    const pageHeaderActionCrumb = {
        label: t("label.create"),
        link: getSubscriptionGroupsCreatePath(),
    };

    return (
        <SubscriptionGroupsWizardPage
            pageHeaderActionCrumb={pageHeaderActionCrumb}
            pageHeaderTitle={pageHeaderTitle}
            submitButtonLabel={t("label.save")}
            onCancel={handleOnCancelClick}
            onFinish={checkQuota}
        />
    );
};
