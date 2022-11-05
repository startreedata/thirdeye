import axios from "axios";
import { removeTrailingForwardSlashV1 } from "../../utils/url/url.util";
import { OpenIDConfigurationV1 } from "../dto/openid-configuration.interfaces";

const BASE_URL_OPENID_CONFIGURATION_V1 = "/.well-known/openid-configuration";

export const getOpenIDConfigurationV1 = async (
    oidcIssuerUrl: string
): Promise<OpenIDConfigurationV1> => {
    const response = await axios.get(
        `${removeTrailingForwardSlashV1(
            oidcIssuerUrl
        )}${BASE_URL_OPENID_CONFIGURATION_V1}`
    );

    return response.data;
};
