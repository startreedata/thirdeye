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
import { FunctionComponent } from "react";
import {
    NotificationSpec,
    SpecType,
    SubscriptionGroup,
} from "../../../../../rest/dto/subscription-group.interfaces";

export interface GroupsEditorProps {
    subscriptionGroup: SubscriptionGroup;
    onSubscriptionGroupEmailsChange: (emails: string[]) => void;
    onSpecsChange: (specs: NotificationSpec[]) => void;
}

export interface SpecUIConfig {
    id: SpecType;
    internationalizationString: string;
    icon: string;
    /**
     * There is no simple way to type the expected props for the different
     * spec configurations
     */
    /* eslint-disable  @typescript-eslint/no-explicit-any */
    formComponent: FunctionComponent<any>;
    /* eslint-disable  @typescript-eslint/no-explicit-any */
    reviewComponent: FunctionComponent<any>;
    /* eslint-disable  @typescript-eslint/no-explicit-any */
    viewComponent: FunctionComponent<any>;
    validate: (spec: NotificationSpec) => boolean;
}
