/**
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import {
    Card,
    CardContent,
    CardHeader,
    Grid,
    IconButton,
    Menu,
    MenuItem,
} from "@material-ui/core";
import MoreVertIcon from "@material-ui/icons/MoreVert";
import { isEmpty, isNumber } from "lodash";
import React, {
    FunctionComponent,
    MouseEvent,
    useEffect,
    useState,
} from "react";
import { useTranslation } from "react-i18next";
import { useNavigate, useParams } from "react-router-dom";
import { NoDataIndicator } from "../../components/no-data-indicator/no-data-indicator.component";
import { PageHeader } from "../../components/page-header/page-header.component";
import {
    AppLoadingIndicatorV1,
    JSONEditorV1,
    NotificationTypeV1,
    PageContentsGridV1,
    PageV1,
    useDialogProviderV1,
    useNotificationProviderV1,
} from "../../platform/components";
import { DialogType } from "../../platform/components/dialog-provider-v1/dialog-provider-v1.interfaces";
import { ActionStatus } from "../../rest/actions.interfaces";
import { useGetAlertTemplate } from "../../rest/alert-templates/alert-templates.actions";
import { deleteAlertTemplate } from "../../rest/alert-templates/alert-templates.rest";
import { AlertTemplate } from "../../rest/dto/alert-template.interfaces";
import {
    getAlertTemplatesAllPath,
    getAlertTemplatesUpdatePath,
} from "../../utils/routes/routes.util";
import { AlertsTemplatesViewPageParams } from "./alert-templates-view-page.interfaces";

export const AlertTemplatesViewPage: FunctionComponent = () => {
    const { showDialog } = useDialogProviderV1();
    const {
        alertTemplate,
        getAlertTemplate,
        status: getAlertRequestStatus,
        errorMessages: alertTemplateErrors,
    } = useGetAlertTemplate();
    const [
        alertTemplateOptionsAnchorElement,
        setAlertTemplateOptionsAnchorElement,
    ] = useState<HTMLElement | null>();
    const { id: alertTemplateId } = useParams<AlertsTemplatesViewPageParams>();
    const { t } = useTranslation();
    const { notify } = useNotificationProviderV1();
    const navigate = useNavigate();

    useEffect(() => {
        if (!isNumber(Number(alertTemplateId))) {
            return;
        }
        getAlertTemplate(Number(alertTemplateId));
    }, [alertTemplateId]);

    useEffect(() => {
        if (getAlertRequestStatus === ActionStatus.Error) {
            isEmpty(alertTemplateErrors)
                ? notify(
                      NotificationTypeV1.Error,
                      t("message.error-while-fetching", {
                          entity: t("label.alert-template"),
                      })
                  )
                : alertTemplateErrors.map((msg) =>
                      notify(NotificationTypeV1.Error, msg)
                  );
        }
    }, [getAlertRequestStatus, alertTemplateErrors]);

    const handleDatasetOptionsClick = (
        event: MouseEvent<HTMLElement>
    ): void => {
        setAlertTemplateOptionsAnchorElement(event.currentTarget);
    };

    const handleAlertTemplateEdit = (): void => {
        if (!alertTemplate) {
            return;
        }

        navigate(getAlertTemplatesUpdatePath(alertTemplate.id));
        setAlertTemplateOptionsAnchorElement(null);
    };

    const handleAlertTemplateDelete = (): void => {
        alertTemplate &&
            showDialog({
                type: DialogType.ALERT,
                contents: t("message.delete-confirmation", {
                    name: alertTemplate.name,
                }),
                okButtonText: t("label.confirm"),
                cancelButtonText: t("label.cancel"),
                onOk: () => handleAlertTemplateDeleteOk(alertTemplate),
            });
    };

    const handleAlertTemplateDeleteOk = (
        alertTemplate: AlertTemplate
    ): void => {
        deleteAlertTemplate(alertTemplate.id).then(() => {
            notify(
                NotificationTypeV1.Success,
                t("message.delete-success", {
                    entity: t("label.alert-template"),
                })
            );

            navigate(getAlertTemplatesAllPath());
        });
    };

    return getAlertRequestStatus === ActionStatus.Working ? (
        <AppLoadingIndicatorV1 />
    ) : (
        <PageV1>
            <PageHeader
                showCreateButton
                title={alertTemplate ? alertTemplate.name : ""}
            />

            <PageContentsGridV1>
                {/* Readonly detection configuration */}
                <Grid item sm={12}>
                    <Card variant="outlined">
                        <CardHeader
                            action={
                                <Grid container alignItems="center" spacing={0}>
                                    <Grid item>
                                        {/* Dataset options button */}
                                        <IconButton
                                            color="secondary"
                                            onClick={handleDatasetOptionsClick}
                                        >
                                            <MoreVertIcon />
                                        </IconButton>

                                        {/* Alert template options */}
                                        <Menu
                                            anchorEl={
                                                alertTemplateOptionsAnchorElement
                                            }
                                            open={Boolean(
                                                alertTemplateOptionsAnchorElement
                                            )}
                                            onClose={() =>
                                                setAlertTemplateOptionsAnchorElement(
                                                    null
                                                )
                                            }
                                        >
                                            {/* Edit alert template */}
                                            <MenuItem
                                                onClick={
                                                    handleAlertTemplateEdit
                                                }
                                            >
                                                {t("label.edit-entity", {
                                                    entity: t(
                                                        "label.alert-template"
                                                    ),
                                                })}
                                            </MenuItem>

                                            {/* Delete alert template */}
                                            <MenuItem
                                                onClick={
                                                    handleAlertTemplateDelete
                                                }
                                            >
                                                {t("label.delete-entity", {
                                                    entity: t(
                                                        "label.alert-template"
                                                    ),
                                                })}
                                            </MenuItem>
                                        </Menu>
                                    </Grid>
                                </Grid>
                            }
                            title={t("label.alert-template")}
                            titleTypographyProps={{ variant: "h6" }}
                        />
                        <CardContent>
                            {alertTemplate && (
                                <JSONEditorV1<AlertTemplate>
                                    hideValidationSuccessIcon
                                    readOnly
                                    value={alertTemplate}
                                />
                            )}

                            {getAlertRequestStatus === ActionStatus.Error && (
                                <NoDataIndicator />
                            )}
                        </CardContent>
                    </Card>
                </Grid>
            </PageContentsGridV1>
        </PageV1>
    );
};
