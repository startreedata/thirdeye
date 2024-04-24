/*
 * Copyright 2024 StarTree Inc
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
type CustomErrorMessage = {
    criteriaFunction: (message: string) => boolean;
    customMessage: string;
};

export const generateCustomErrorMessage = (
    errorMessages: string[],
    customErrorMessages: CustomErrorMessage[]
): string[] => {
    const newErrorMessages = [...errorMessages];
    customErrorMessages.forEach((customErrorMessage) => {
        const index = newErrorMessages.findIndex((message) =>
            customErrorMessage.criteriaFunction(message)
        );
        if (index !== -1) {
            newErrorMessages[index] = customErrorMessage.customMessage;
        }
    });

    return newErrorMessages;
};
