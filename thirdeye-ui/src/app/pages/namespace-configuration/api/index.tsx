/*
 * Copyright 2024 StarTree Inc
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
import { useEffect, useState } from "react";
import { ActionStatus } from "../../../rest/actions.interfaces";
import {
    useGetWorkspaceConfiguration,
    useResetWorkspaceConfiguration,
    useUpdateWorkspaceConfiguration,
} from "../../../rest/workspace/workspace-action";
import { isEmpty, isEqual } from "lodash";
import { notifyIfErrors } from "../../../utils/notifications/notifications.util";
import { useNotificationProviderV1 } from "../../../platform/components";
import { useTranslation } from "react-i18next";
import { WorkspaceConfiguration } from "../../../rest/dto/workspace.interfaces";

type namespaceConfig = {
    timezone: string | undefined;
    dateTimePattern: string | undefined;
};

type WorkspaceApiReques = {
    isError: boolean;
    isLoading: boolean;
    isUpdateDisabled: boolean;
    namespaceConfig: namespaceConfig;
    workspaceConfiguration: WorkspaceConfiguration | null;
    setNamespaceConfig: (config: namespaceConfig) => void;
    getWorkspaceConfiguration: () => Promise<
        WorkspaceConfiguration | undefined
    >;
    resetWorkspaceConfiguration: () => Promise<
        WorkspaceConfiguration | undefined
    >;
    updateWorkspaceConfiguration: (
        config: WorkspaceConfiguration
    ) => Promise<WorkspaceConfiguration | undefined>;
};

export const useWorkspaceApiRequests = (): WorkspaceApiReques => {
    const [namespaceConfig, setNamespaceConfig] = useState<namespaceConfig>({
        timezone: "",
        dateTimePattern: "",
    });

    const { notify } = useNotificationProviderV1();
    const { t } = useTranslation();

    const {
        workspaceConfiguration,
        getWorkspaceConfiguration,
        status: workspaceStatus,
        errorMessages: workspaceErrorMessages,
    } = useGetWorkspaceConfiguration();

    const {
        workspaceConfiguration: defaultWorkspaceConfiguration,
        resetWorkspaceConfiguration,
        status: resetStaus,
        errorMessages: resetErrorMessages,
    } = useResetWorkspaceConfiguration();

    const {
        workspaceConfiguration: updatedWorkspaceConfiguration,
        updateWorkspaceConfiguration,
        status: updateStatus,
        errorMessages: updateErrorMessages,
    } = useUpdateWorkspaceConfiguration();

    useEffect(() => {
        getWorkspaceConfiguration();
    }, []);

    useEffect(() => {
        if (!isEmpty(workspaceConfiguration)) {
            setNamespaceConfig({
                timezone: workspaceConfiguration?.timeConfiguration.timezone,
                dateTimePattern:
                    workspaceConfiguration?.timeConfiguration.dateTimePattern,
            });
        }
    }, [workspaceConfiguration]);

    useEffect(() => {
        if (!isEmpty(updatedWorkspaceConfiguration)) {
            setNamespaceConfig({
                timezone:
                    updatedWorkspaceConfiguration?.timeConfiguration.timezone,
                dateTimePattern:
                    updatedWorkspaceConfiguration?.timeConfiguration
                        .dateTimePattern,
            });
        }
    }, [updatedWorkspaceConfiguration]);

    useEffect(() => {
        if (!isEmpty(defaultWorkspaceConfiguration)) {
            setNamespaceConfig({
                timezone:
                    defaultWorkspaceConfiguration?.timeConfiguration.timezone,
                dateTimePattern:
                    defaultWorkspaceConfiguration?.timeConfiguration
                        .dateTimePattern,
            });
        }
    }, [defaultWorkspaceConfiguration]);

    useEffect(() => {
        notifyIfErrors(
            workspaceStatus,
            workspaceErrorMessages,
            notify,
            t("message.error-while-fetching", {
                entity: t("label.workspace-config"),
            })
        );
    }, [workspaceStatus]);

    useEffect(() => {
        notifyIfErrors(
            resetStaus,
            resetErrorMessages,
            notify,
            t("message.update-error", {
                entity: t("label.workspace-config"),
            })
        );
    }, [resetStaus]);

    useEffect(() => {
        notifyIfErrors(
            updateStatus,
            updateErrorMessages,
            notify,
            t("message.update-error", {
                entity: t("label.workspace-config"),
            })
        );
    }, [updateStatus]);

    const isError = workspaceStatus === ActionStatus.Error;

    const isLoading =
        workspaceStatus === ActionStatus.Working ||
        updateStatus === ActionStatus.Working ||
        resetStaus === ActionStatus.Working;

    const isUpdateDisabled = isEqual(namespaceConfig, {
        dateTimePattern:
            workspaceConfiguration?.timeConfiguration.dateTimePattern,
        timezone: workspaceConfiguration?.timeConfiguration.timezone,
    });

    return {
        isError,
        isLoading,
        isUpdateDisabled,
        namespaceConfig,
        workspaceConfiguration,
        setNamespaceConfig,
        getWorkspaceConfiguration,
        resetWorkspaceConfiguration,
        updateWorkspaceConfiguration,
    };
};
