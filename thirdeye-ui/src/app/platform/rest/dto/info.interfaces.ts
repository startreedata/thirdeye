import { OpenIDConfigurationV1 } from "./openid-configuration.interfaces";

export interface InfoV1 {
    oidcIssuerUrl: string;
    openidConfiguration: OpenIDConfigurationV1;
}
