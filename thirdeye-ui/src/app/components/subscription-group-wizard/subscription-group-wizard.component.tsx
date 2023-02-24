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

import React, { FunctionComponent, useEffect, useState } from "react";
import { PageContentsGridV1 } from "../../platform/components";
import {
    AlertAssociation,
    SubscriptionGroup,
} from "../../rest/dto/subscription-group.interfaces";
import { WizardBottomBar } from "../welcome-onboard-datasource/wizard-bottom-bar/wizard-bottom-bar.component";
import { AlertsDimensions } from "./alerts-dimensions/alerts-dimensions.component";
import { SubscriptionGroupDetails } from "./subscription-group-details/subscription-group-details.component";
import {
    Association,
    SubscriptionGroupViewTabs,
    SubscriptionGroupWizardProps,
} from "./subscription-group-wizard.interfaces";
import {
    getAssociations,
    validateSubscriptionGroup,
} from "./subscription-group-wizard.utils";

export const SubscriptionGroupWizard: FunctionComponent<SubscriptionGroupWizardProps> =
    ({
        alerts,
        submitBtnLabel,
        cancelBtnLabel,
        subscriptionGroup,
        enumerationItems,
        onCancel,
        onFinish,
        selectedTab,
    }) => {
        const [editedSubscriptionGroup, setEditedSubscriptionGroup] =
            useState<SubscriptionGroup>(subscriptionGroup);

        const [editedAssociations, setEditedAssociations] = useState<
            Association[]
        >(getAssociations(subscriptionGroup));

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
                <PageContentsGridV1>
                    {selectedTab === SubscriptionGroupViewTabs.GroupDetails && (
                        <SubscriptionGroupDetails
                            subscriptionGroup={editedSubscriptionGroup}
                            onChange={setEditedSubscriptionGroup}
                        />
                    )}
                    {selectedTab ===
                        SubscriptionGroupViewTabs.AlertDimensions && (
                        <AlertsDimensions
                            alerts={alerts}
                            associations={editedAssociations}
                            enumerationItems={enumerationItems}
                            setAssociations={setEditedAssociations}
                        />
                    )}
                </PageContentsGridV1>
                <WizardBottomBar
                    backButtonLabel={cancelBtnLabel}
                    handleBackClick={onCancel}
                    handleNextClick={handleSubmitClick}
                    nextButtonIsDisabled={!isSubscriptionGroupValid}
                    nextButtonLabel={submitBtnLabel}
                />
            </>
        );
    };
