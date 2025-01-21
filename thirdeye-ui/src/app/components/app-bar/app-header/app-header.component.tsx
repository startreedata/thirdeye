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
import { Link, Tooltip, Typography } from "@material-ui/core";
import { AppHeaderprops } from "./app-header.inerfaces";
import OpenInNewIcon from "@material-ui/icons/OpenInNew";
import { useTranslation } from "react-i18next";
import { useNavBarV1 } from "../../../platform/stores/nav-bar-v1/nav-bar-v1.store";
import { WorkspaceSwitcher } from "./workspace";
import InfoIconOutlined from "@material-ui/icons/InfoOutlined";

export const Appheader = ({
    isFullScreen,
    workspaces,
    selectedWorkspace,
    onWorkspaceChange,
    quota,
}: AppHeaderprops): ReactElement => {
    const { t } = useTranslation();
    const [navBarMinimized] = useNavBarV1((state) => [state.navBarMinimized]);
    const showWorkspaceSwitcher = Boolean(workspaces);
    const compoenentStyles = useAppHeaderStyles({
        showWorkspaceSwitcher,
        isFullScreen,
        navBarMinimized,
    })();

    const getQuotaString = (
        type: "detection" | "notification"
    ): string | undefined => {
        if (quota) {
            if (type === "detection") {
                return `${Number(
                    (
                        (quota.remainingQuota.detection /
                            quota.totalQuota.detection) *
                        100
                    ).toFixed(2)
                )}%(${quota.remainingQuota.detection})`;
            } else {
                return `${Number(
                    (
                        (quota.remainingQuota.notification /
                            quota.totalQuota.notification) *
                        100
                    ).toFixed(2)
                )}%(${quota.remainingQuota.notification})`;
            }
        }

        return;
    };

    return (
        <div className={compoenentStyles.header}>
            {showWorkspaceSwitcher && (
                <WorkspaceSwitcher
                    selectedWorkspace={selectedWorkspace}
                    workspaces={workspaces!}
                    onWorkspaceChange={onWorkspaceChange}
                />
            )}
            <div className={compoenentStyles.rightInfoSpace}>
                {quota && (
                    <div className={compoenentStyles.taskInfoContainer}>
                        <Typography variant="body2">Remaining Quota</Typography>
                        <Tooltip
                            arrow
                            interactive
                            placement="top"
                            title={
                                <Typography variant="caption">
                                    {/* <ParseMarkdown {...parseMarkdownProps}> */}
                                    {t("message.quota-usage")}
                                    {/* </ParseMarkdown> */}
                                </Typography>
                            }
                        >
                            <InfoIconOutlined
                                color="secondary"
                                fontSize="small"
                            />
                        </Tooltip>
                        :
                        <div className={compoenentStyles.taskInfo}>
                            <div>Detection: {getQuotaString("detection")}</div>
                            <div>
                                Notification: {getQuotaString("notification")}
                            </div>
                        </div>
                    </div>
                )}
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
