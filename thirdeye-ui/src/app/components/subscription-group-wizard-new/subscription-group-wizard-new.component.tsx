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

import { Box, Button, Grid } from "@material-ui/core";
import React, { FunctionComponent, useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { useParams, useSearchParams } from "react-router-dom";
import {
    PageContentsCardV1,
    PageContentsGridV1,
} from "../../platform/components";
import {
    AlertAssociation,
    SubscriptionGroup,
} from "../../rest/dto/subscription-group.interfaces";
import {
    getConfigurationPath,
    getSubscriptionGroupsAllPath,
    getSubscriptionGroupsCreatePath,
    getSubscriptionGroupsUpdatePath,
} from "../../utils/routes/routes.util";
import { PageHeader } from "../page-header/page-header.component";
import { PageHeaderProps } from "../page-header/page-header.interfaces";
import { AlertsDimensions } from "./alerts-dimensions/alerts-dimensions.component";
import { SubscriptionGroupDetails } from "./subscription-group-details/subscription-group-details.component";
import {
    Association,
    SubscriptionGroupsWizardParams,
    SubscriptionGroupViewTabs,
    SubscriptionGroupWizardNewProps,
} from "./subscription-group-wizard-new.interface";
import {
    getAssociations,
    SelectedTab,
    validateSubscriptionGroup,
} from "./subscription-group-wizard-new.utils";

export const SubscriptionGroupWizardNew: FunctionComponent<SubscriptionGroupWizardNewProps> =
    ({
        alerts,
        submitBtnLabel,
        subscriptionGroup,
        enumerationItems,
        onCancel,
        onFinish,
        isExisting = false,
    }) => {
        const [editedSubscriptionGroup, setEditedSubscriptionGroup] =
            useState<SubscriptionGroup>(subscriptionGroup);

        const [editedAssociations, setEditedAssociations] = useState<
            Association[]
        >(getAssociations(subscriptionGroup));

        const { t } = useTranslation();

        const params = useParams<SubscriptionGroupsWizardParams>();
        const [searchParams] = useSearchParams();
        const [selectedTab] = useMemo(
            () => [
                Number(searchParams.get(SelectedTab)) ||
                    SubscriptionGroupViewTabs.GroupDetails,
            ],
            [searchParams]
        );

        const id = isExisting ? Number(params.id) : null;

        const pagePath = isExisting
            ? getSubscriptionGroupsUpdatePath(Number(params.id))
            : getSubscriptionGroupsCreatePath();

        const pageHeaderProps: PageHeaderProps = {
            breadcrumbs: [
                {
                    label: t("label.configuration"),
                    link: getConfigurationPath(),
                },
                {
                    label: t("label.subscription-groups"),
                    link: getSubscriptionGroupsAllPath(),
                },
                {
                    label: isExisting && id ? id : t("label.create"),
                    link: pagePath,
                },
            ],
            transparentBackground: true,
            title: t(`label.${isExisting ? "update" : "create"}-entity`, {
                entity: t("label.subscription-group"),
            }),
            subNavigation: [
                {
                    label: t("label.group-details"),
                    link: `${pagePath}?${SelectedTab}=${SubscriptionGroupViewTabs.GroupDetails}`,
                },
                {
                    label: t("label.alerts-and-dimensions"),
                    link: `${pagePath}?${SelectedTab}=${SubscriptionGroupViewTabs.AlertDimensions}`,
                },
            ],
            subNavigationSelected: selectedTab,
        };

        const handleSubmitClick = (): void => {
            onFinish?.(editedSubscriptionGroup);
        };

        useEffect(() => {
            const newSubscriptionGroupAssociations: AlertAssociation[] =
                editedAssociations.map(({ alertId, enumerationId }) => ({
                    alert: { id: alertId },
                    ...(enumerationId && {
                        enumerationItem: { id: enumerationId },
                    }),
                }));

            // Remove the @deprecated `alerts` key from existing data
            // eslint-disable-next-line @typescript-eslint/no-unused-vars
            setEditedSubscriptionGroup(({ alerts, ...stateProp }) => ({
                ...stateProp,
                alertAssociations: newSubscriptionGroupAssociations,
            }));
        }, [editedAssociations]);

        const isSubscriptionGroupValid = validateSubscriptionGroup(
            editedSubscriptionGroup
        );

        return (
            <>
                <PageHeader {...pageHeaderProps} />

                <PageContentsGridV1>
                    {selectedTab === SubscriptionGroupViewTabs.GroupDetails ? (
                        <SubscriptionGroupDetails
                            subscriptionGroup={editedSubscriptionGroup}
                            onChange={setEditedSubscriptionGroup}
                        />
                    ) : null}
                    {selectedTab ===
                    SubscriptionGroupViewTabs.AlertDimensions ? (
                        <AlertsDimensions
                            alerts={alerts}
                            associations={editedAssociations}
                            enumerationItems={enumerationItems}
                            setAssociations={setEditedAssociations}
                        />
                    ) : null}
                </PageContentsGridV1>
                <Box textAlign="right" width="100%">
                    <PageContentsCardV1 fullHeight>
                        <Grid container justifyContent="flex-end">
                            <Grid item>
                                <Button
                                    color="secondary"
                                    variant="outlined"
                                    onClick={() => onCancel?.()}
                                >
                                    {t("label.cancel")}
                                </Button>
                            </Grid>
                            <Grid item>
                                <Button
                                    color="primary"
                                    disabled={!isSubscriptionGroupValid}
                                    onClick={handleSubmitClick}
                                >
                                    {submitBtnLabel || isExisting
                                        ? t("label.save")
                                        : t("label.create")}
                                </Button>
                            </Grid>
                        </Grid>
                    </PageContentsCardV1>
                </Box>
            </>
        );
    };
