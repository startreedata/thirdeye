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
import { delay, isEmpty } from "lodash";
import * as React from "react";
import {
    createContext,
    FunctionComponent,
    useContext,
    useEffect,
    useState,
} from "react";
import { AppBar } from "../app-bar.component";
import {
    AppBarConfigProviderProps,
    AppBarConfigProviderPropsContextProps,
} from "./app-bar-config-provider.interface";
import { Appheader } from "../app-header/app-header.component";
import { useStyle } from "./app-bar.styles";
import { useGetWorkspaces } from "../../../rest/workspace/workspace-action";
import { useAuthV1 } from "../../../platform/stores/auth-v1/auth-v1.store";
import { ActionStatus } from "../../../rest/actions.interfaces";
import { notifyIfErrors } from "../../../utils/notifications/notifications.util";
import { useTranslation } from "react-i18next";
import {
    useAuthProviderV1,
    useNotificationProviderV1,
} from "../../../platform/components";
import { useUserPreferences } from "../../../utils/user-preferences/user-preferences";
import { UserPreferencesKeys } from "../../../utils/user-preferences/user-preferences.interfaces";

export const AppBarConfigProvider: FunctionComponent<AppBarConfigProviderProps> =
    ({ children }) => {
        const { t } = useTranslation();
        const { notify } = useNotificationProviderV1();
        const { setWorkspace, workspace } = useAuthV1();
        const { authenticated, authDisabled } = useAuthProviderV1();
        const { setPreference, localPreferences } = useUserPreferences();
        const [showAppNavBar, setShowAppNavBar] = useState(true);
        const [okToRender, setOkToRender] = useState(false);
        const [mainViewKey, setMainViewKey] = useState<string | null>(
            "default"
        );
        const [mappedWorkspaces, setMappedWorkspaes] =
            useState<{ id: string }[]>();
        const compoenentStyles = useStyle();

        const { workspaces, getWorkspaces, status, errorMessages } =
            useGetWorkspaces();
        const isLoading =
            status === ActionStatus.Initial || status === ActionStatus.Working;

        useEffect(() => {
            getWorkspaces();
        }, []);

        useEffect(() => {
            if (!isEmpty(workspaces)) {
                /* If the workspace name is not set, we get null as the workspace id,
                so we map it to a default name to be shown on the UI
                */
                setMappedWorkspaes(
                    workspaces?.map((workspace) => {
                        if (workspace.id) {
                            return { id: workspace.id };
                        } else {
                            return {
                                id: t("label.app-header.null-mapped-name"),
                            };
                        }
                    })
                );
                // Check if there is a workspace present in localStorage and whether its a valid
                // workspace by checking it against fetched workspaces. If its present set it as
                // workspace for current session else set the first workpsace from fetched data.
                const sessionWorkspace = localPreferences.currentWorkspace;
                const validSessionWorkspace = workspaces?.find((workspace) => {
                    if (
                        sessionWorkspace ===
                            t("label.app-header.null-mapped-name") &&
                        workspace.id === null
                    ) {
                        return true;
                    } else {
                        return workspace.id === sessionWorkspace;
                    }
                });
                validSessionWorkspace
                    ? setWorkspace(validSessionWorkspace)
                    : setWorkspace(workspaces![0]);
            }
        }, [workspaces]);

        const handleWorkspaceChange = (workspace: { id: string }): void => {
            // store the workspace in localStorage
            setPreference(UserPreferencesKeys.CURRENT_WORKSPACE, workspace.id);
            if (workspace.id === t("label.app-header.null-mapped-name")) {
                setWorkspace({ id: null });
            } else {
                setWorkspace(workspace);
            }
        };

        useEffect(() => {
            /* Using timeout here because we want to re-render once the axios interceptor has been
            updated with namespace header value, so that API calls will use the updated header.
            */
            setTimeout(() => {
                setMainViewKey(workspace.id);
            });
        }, [workspace]);

        useEffect(() => {
            // Slight delay in rendering nav bar helps avoid flicker during initial page redirects
            delay(setOkToRender, 200, true);
        }, []);

        useEffect(() => {
            notifyIfErrors(
                status,
                errorMessages,
                notify,
                t("message.error-while-fetching", {
                    entity: t("label.app-header.workspace"),
                })
            );
        }, [status]);

        let mappedSelectedWorkspace: { id: string };
        if (workspace.id === null) {
            mappedSelectedWorkspace = {
                id: t("label.app-header.null-mapped-name"),
            };
        } else {
            mappedSelectedWorkspace = { id: workspace.id };
        }

        const showNavbar = okToRender && showAppNavBar;
        const showAuthBasedView = authDisabled || authenticated;

        return (
            <AppBarConfigProviderContext.Provider value={{ setShowAppNavBar }}>
                {showNavbar && showAuthBasedView && <AppBar />}
                <div className={compoenentStyles.rightView}>
                    {showAuthBasedView && (
                        <Appheader
                            isFullScreen={!showNavbar}
                            selectedWorkspace={mappedSelectedWorkspace}
                            workspaces={mappedWorkspaces}
                            onWorkspaceChange={handleWorkspaceChange}
                        />
                    )}
                    <div
                        className={compoenentStyles.mainContent}
                        key={mainViewKey}
                    >
                        {!isLoading && children}
                    </div>
                </div>
            </AppBarConfigProviderContext.Provider>
        );
    };

const AppBarConfigProviderContext =
    createContext<AppBarConfigProviderPropsContextProps>(
        {} as AppBarConfigProviderPropsContextProps
    );

export const useAppBarConfigProvider =
    (): AppBarConfigProviderPropsContextProps => {
        return useContext(AppBarConfigProviderContext);
    };
