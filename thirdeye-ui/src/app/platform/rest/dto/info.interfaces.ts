// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
import { OpenIDConfigurationV1 } from "./openid-configuration.interfaces";

export interface InfoV1 {
    oidcIssuerUrl: string;
    openidConfiguration: OpenIDConfigurationV1;
}
