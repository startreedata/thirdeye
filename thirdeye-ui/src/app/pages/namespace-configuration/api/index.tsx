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

type WorkspaceApiReques = {
    isError: boolean;
    isLoading: boolean;
    isUpdateDisabled: boolean;
    namespaceConfig: WorkspaceConfiguration | null;
    workspaceConfiguration: WorkspaceConfiguration | null;
    setNamespaceConfig: (config: WorkspaceConfiguration) => void;
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
    const [namespaceConfig, setNamespaceConfig] =
        useState<WorkspaceConfiguration | null>(null);

    const { notify } = useNotificationProviderV1();
    const { t } = useTranslation();

    const {
        workspaceConfiguration,
        getWorkspaceConfiguration,
        status: workspaceStatus,
        errorMessages: workspaceErrorMessages,
    } = useGetWorkspaceConfiguration();

    const {
        resetWorkspaceConfiguration,
        status: resetStaus,
        errorMessages: resetErrorMessages,
    } = useResetWorkspaceConfiguration();

    const {
        updateWorkspaceConfiguration,
        status: updateStatus,
        errorMessages: updateErrorMessages,
    } = useUpdateWorkspaceConfiguration();

    useEffect(() => {
        getWorkspaceConfiguration();
    }, []);

    useEffect(() => {
        if (!isEmpty(workspaceConfiguration)) {
            setNamespaceConfig(workspaceConfiguration);
        }
    }, [workspaceConfiguration]);

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
        if (resetStaus === ActionStatus.Done) {
            getWorkspaceConfiguration();
        }
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
        if (updateStatus === ActionStatus.Done) {
            getWorkspaceConfiguration();
        }
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

    const isUpdateDisabled = isEqual(namespaceConfig, workspaceConfiguration);

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
