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
import axios from "axios";
import {
    AlertTemplate,
    NewAlertTemplate,
} from "../dto/alert-template.interfaces";

const BASE_URL_ALERTS = "/api/alert-templates";

export const getAlertTemplate = async (id: number): Promise<AlertTemplate> => {
    const response = await axios.get(`${BASE_URL_ALERTS}/${id}`);

    return response.data;
};

export const getAlertTemplates = async (): Promise<AlertTemplate[]> => {
    const response = await axios.get(BASE_URL_ALERTS);

    return response.data;
};

export const createAlertTemplate = async (
    alertTemplate: AlertTemplate | NewAlertTemplate
): Promise<AlertTemplate> => {
    const response = await axios.post(BASE_URL_ALERTS, [alertTemplate]);

    return response.data[0];
};

export const updateAlertTemplate = async (
    alertTemplate: AlertTemplate
): Promise<AlertTemplate> => {
    const response = await axios.put(BASE_URL_ALERTS, [alertTemplate]);

    return response.data[0];
};

export const deleteAlertTemplate = async (
    id: number
): Promise<AlertTemplate> => {
    const response = await axios.delete(`${BASE_URL_ALERTS}/${id}`);

    return response.data;
};
