// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
import { ActionHook } from "../actions.interfaces";
import { OpenIDConfigurationV1 } from "../dto/openid-configuration.interfaces";

export interface GetOpenIDConfigurationV1 extends ActionHook {
    openIDConfigurationV1: OpenIDConfigurationV1 | null;
    getOpenIDConfigurationV1: (oidcIssuerUrl: string) => Promise<void>;
}
