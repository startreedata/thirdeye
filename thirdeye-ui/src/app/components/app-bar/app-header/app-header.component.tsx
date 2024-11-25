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
import React, { ReactElement } from "react";
import { useAppHeaderStyles } from "./app-header.styles";
import { Link } from "@material-ui/core";
import { AppHeaderprops } from "./app-header.inerfaces";
import OpenInNewIcon from "@material-ui/icons/OpenInNew";
import { useTranslation } from "react-i18next";
import { useNavBarV1 } from "../../../platform/stores/nav-bar-v1/nav-bar-v1.store";
import { WorkspaceSwitcher } from "./workspace";

export const Appheader = ({
    isFullScreen,
    workspaces,
    selectedWorkspace,
    onWorkspaceChange,
}: AppHeaderprops): ReactElement => {
    const { t } = useTranslation();
    const [navBarMinimized] = useNavBarV1((state) => [state.navBarMinimized]);
    const showWorkspaceSwitcher = Boolean(workspaces && workspaces?.length > 1);
    const compoenentStyles = useAppHeaderStyles({
        showWorkspaceSwitcher,
        isFullScreen,
        navBarMinimized,
    })();

    return (
        <div className={compoenentStyles.header}>
            {showWorkspaceSwitcher && (
                <WorkspaceSwitcher
                    selectedWorkspace={selectedWorkspace}
                    workspaces={workspaces!}
                    onWorkspaceChange={onWorkspaceChange}
                />
            )}
            <div>
                <Link href="https://startree.cloud/" target="_blank">
                    <div className={compoenentStyles.button}>
                        <div>{t("label.app-header.startree-cloud")}</div>
                        <OpenInNewIcon
                            className={compoenentStyles.icon}
                            fontSize="small"
                        />
                    </div>
                </Link>
            </div>
        </div>
    );
};
