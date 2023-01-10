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
import { Button } from "@material-ui/core";
import {
    default as React,
    FunctionComponent,
    useCallback,
    useEffect,
    useMemo,
    useState,
} from "react";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";
import { createNewStartingAlert } from "../../../components/alert-wizard-v2/alert-template/alert-template.utils";
import { useAppBarConfigProvider } from "../../../components/app-bar/app-bar-config-provider/app-bar-config-provider.component";
import { PageHeader } from "../../../components/page-header/page-header.component";
import { LoadingErrorStateSwitch } from "../../../components/page-states/loading-error-state-switch/loading-error-state-switch.component";
import { generateEmptyEmailSendGridConfiguration } from "../../../components/subscription-group-wizard/groups-editor/groups-editor.utils";
import {
    NotificationTypeV1,
    PageV1,
    useNotificationProviderV1,
} from "../../../platform/components";
import { ActionStatus } from "../../../rest/actions.interfaces";
import { useGetAlertTemplates } from "../../../rest/alert-templates/alert-templates.actions";
import { EditableAlert } from "../../../rest/dto/alert.interfaces";
import {
    SpecType,
    SubscriptionGroup,
} from "../../../rest/dto/subscription-group.interfaces";
import { useCreateSubscriptionGroup } from "../../../rest/subscription-groups/subscription-groups.actions";
import {
    handleAlertPropertyChangeGenerator,
    handleCreateAlertClickGenerator,
} from "../../../utils/anomalies/anomalies.util";
import { QUERY_PARAM_KEYS } from "../../../utils/constants/constants.util";
import { getHomePath } from "../../../utils/routes/routes.util";
import { createEmptySubscriptionGroup } from "../../../utils/subscription-groups/subscription-groups.util";
import { CreateAlertGuidedPage } from "../../alerts-create-guided-page/alerts-create-guided-page.component";

export const CreateAlertPage: FunctionComponent = () => {
    const { t } = useTranslation();
    const navigate = useNavigate();
    const { notify } = useNotificationProviderV1();
    const { setShowAppNavBar } = useAppBarConfigProvider();

    const {
        alertTemplates,
        getAlertTemplates,
        status: alertTemplatesRequestStatus,
    } = useGetAlertTemplates();

    const {
        createNewSubscriptionGroup,
        status: createSubscriptionGroupStatus,
    } = useCreateSubscriptionGroup();

    const [alert, setAlert] = useState<EditableAlert>(createNewStartingAlert());
    const [singleNewSubscriptionGroup, setSingleNewSubscriptionGroup] =
        useState<SubscriptionGroup>(() => {
            const subscriptionGroup = createEmptySubscriptionGroup();
            const newEmailConfiguration =
                generateEmptyEmailSendGridConfiguration();
            newEmailConfiguration.params.emailRecipients.to = emails;
            subscriptionGroup.specs = [newEmailConfiguration];

            return subscriptionGroup;
        });
    const [emails, setEmails] = useState<string[]>([]);
    const [createAlertStatus, setCreateAlertStatus] = useState<ActionStatus>(
        ActionStatus.Initial
    );

    const handleAlertPropertyChange = useMemo(() => {
        const localHandleAlertPropertyChange =
            handleAlertPropertyChangeGenerator(
                setAlert,
                [],
                () => {
                    return;
                },
                t
            );

        return (
            contentsToReplace: Partial<EditableAlert>,
            isTotalReplace = false
        ): void => {
            localHandleAlertPropertyChange(contentsToReplace, isTotalReplace);
        };
    }, [setAlert]);

    const createAndHandleSubscriptionGroup = useMemo(() => {
        const queryParams = new URLSearchParams([
            [QUERY_PARAM_KEYS.SHOW_FIRST_ALERT_SUCCESS, "true"],
        ]);

        return handleCreateAlertClickGenerator(notify, t, () => {
            navigate(`${getHomePath()}?${queryParams.toString()}`);
            setShowAppNavBar(true);
        });
    }, [navigate, notify, t]);

    const handleCreateAlertClick = useCallback(
        (_, suggestedName) => {
            const alertWithName = { ...alert };

            if (!alertWithName.name) {
                alertWithName.name = suggestedName;
            }

            /**
             * Create an email only subscription group if the emails
             * list is not empty
             */
            if (
                singleNewSubscriptionGroup &&
                singleNewSubscriptionGroup.specs.length === 1 &&
                singleNewSubscriptionGroup.specs[0].type ===
                    SpecType.EmailSendgrid &&
                singleNewSubscriptionGroup.specs[0].params.emailRecipients.to &&
                singleNewSubscriptionGroup.specs[0].params.emailRecipients.to
                    .length > 0
            ) {
                singleNewSubscriptionGroup.name = `${alertWithName.name}_emails_subscription_group`;

                createNewSubscriptionGroup(singleNewSubscriptionGroup)
                    .then((savedSubscriptionGroup: SubscriptionGroup): void => {
                        createAndHandleSubscriptionGroup(
                            alertWithName,
                            [savedSubscriptionGroup],
                            setCreateAlertStatus
                        );
                    })
                    .catch((): void => {
                        createAndHandleSubscriptionGroup(
                            alertWithName,
                            [],
                            setCreateAlertStatus
                        );
                        notify(
                            NotificationTypeV1.Error,
                            t(
                                "message.skipped-creating-a-new-subscription-group-error"
                            )
                        );
                    });
            } else {
                createAndHandleSubscriptionGroup(
                    alertWithName,
                    [],
                    setCreateAlertStatus
                );
            }
        },
        [
            alert,
            createAndHandleSubscriptionGroup,
            emails,
            singleNewSubscriptionGroup,
        ]
    );

    useEffect(() => {
        getAlertTemplates();
    }, []);

    return (
        <PageV1>
            <PageHeader
                transparentBackground
                customActions={
                    <Button
                        color="primary"
                        href={t("url.documentation-homepage")}
                        target="_blank"
                        variant="contained"
                    >
                        {t("label.help")}
                    </Button>
                }
                subtitle={t("message.by-creating-an-alert-youll-be-able")}
                title={t("message.lets-create-your-first-alert")}
            />

            <LoadingErrorStateSwitch
                wrapInCard
                wrapInGrid
                isError={false}
                isLoading={alertTemplatesRequestStatus === ActionStatus.Working}
            >
                <CreateAlertGuidedPage
                    alert={alert}
                    alertTemplates={alertTemplates || []}
                    emails={emails}
                    getAlertTemplates={getAlertTemplates}
                    isCreatingAlert={
                        createSubscriptionGroupStatus ===
                            ActionStatus.Working ||
                        createAlertStatus === ActionStatus.Working
                    }
                    newSubscriptionGroup={singleNewSubscriptionGroup}
                    setEmails={setEmails}
                    onAlertPropertyChange={handleAlertPropertyChange}
                    onNewSubscriptionGroupChange={setSingleNewSubscriptionGroup}
                    onSubmit={handleCreateAlertClick}
                />
            </LoadingErrorStateSwitch>
        </PageV1>
    );
};
