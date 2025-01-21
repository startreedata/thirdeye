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
import React, { ReactElement, ReactNode } from "react";
import { useAppHeaderStyles } from "./app-header.styles";
import { Link, Tooltip, Typography } from "@material-ui/core";
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

    const getQuotaOverText = (): NonNullable<ReactNode> => {
        let limitText1 = "";
        let limitText2 = "";
        if (
            quota.remainingQuota.notification <= 0 &&
            quota.remainingQuota.detection <= 0
        ) {
            limitText1 = `${quota.totalQuota.detection} detections and ${quota.totalQuota.notification} notifications`;
            limitText2 = "detections and notifications";
        } else if (quota.remainingQuota.notification <= 0) {
            limitText1 = `${quota.totalQuota.notification} notifications`;
            limitText2 = "notifications";
        } else if (quota.remainingQuota.detection <= 0) {
            limitText1 = `${quota.totalQuota.detection} detections`;
            limitText2 = "detections";
        }

        return (
            <div className={compoenentStyles.taskInfoPopover}>
                <div>
                    <Typography variant="caption">
                        You&apos;ve reached your monthly limit of {limitText1}{" "}
                        per month.
                    </Typography>
                </div>
                <br />
                <div>
                    <Typography variant="caption">
                        Your quota will be reset at the end of the month. Until
                        then, {limitText2} will not run. To increase your quotas
                        please reach out to support.
                    </Typography>
                </div>
            </div>
        );
    };

    const showQuotaInfo =
        quota?.remainingQuota?.notification <= 0 ||
        quota?.remainingQuota?.detection <= 0;

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
                {showQuotaInfo && (
                    <div className={compoenentStyles.taskInfoContainer}>
                        <Tooltip
                            arrow
                            interactive
                            placement="bottom-start"
                            title={getQuotaOverText()}
                        >
                            <div className={compoenentStyles.taskInfo}>
                                <div className="label">
                                    You&apos;ve hit your Monthly Task Quota(i)
                                </div>
                            </div>
                        </Tooltip>
                    </div>
                )}
                <Link href="https://startree.cloud/new-user" target="_blank">
                    <div className={compoenentStyles.button}>
                        <div>{t("label.app-header.startree-console")}</div>
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
