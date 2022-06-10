///
/// Copyright 2022 StarTree Inc
///
/// Licensed under the StarTree Community License (the "License"); you may not use
/// this file except in compliance with the License. You may obtain a copy of the
/// License at http://www.startree.ai/legal/startree-community-license
///
/// Unless required by applicable law or agreed to in writing, software distributed under the
/// License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
/// either express or implied.
/// See the License for the specific language governing permissions and limitations under
/// the License.
///


// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
import { ActionHook } from "../actions.interfaces";
import { OpenIDConfigurationV1 } from "../dto/openid-configuration.interfaces";

export interface GetOpenIDConfigurationV1 extends ActionHook {
    openIDConfigurationV1: OpenIDConfigurationV1 | null;
    getOpenIDConfigurationV1: (oidcIssuerUrl: string) => Promise<void>;
}
