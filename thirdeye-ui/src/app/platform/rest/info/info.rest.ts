// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
import axios from "axios";
import { InfoV1 } from "../dto/info.interfaces";

const BASE_URL_INFO_V1 = "/api/info";

export const getInfoV1 = async (): Promise<InfoV1> => {
    const response = await axios.get(BASE_URL_INFO_V1);

    return response.data;
};
