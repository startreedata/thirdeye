/*
 * Copyright 2023 StarTree Inc
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
import { NotificationTypeV1 } from "../../platform/components";
import { ErrorMessage } from "../../platform/components/notification-provider-v1/notification-provider-v1/notification-provider-v1.interfaces";
import { ActionStatus } from "../../rest/actions.interfaces";
import { notifyIfErrors } from "./notifications.util";

describe("Notifications Util", () => {
    it("notifyIfErrors should not call notify if request status is not Error", () => {
        const mockNotify = jest.fn();
        const errorMessages: ErrorMessage[] = [
            {
                message: "bar",
            },
        ];
        notifyIfErrors(ActionStatus.Initial, errorMessages, mockNotify, "foo");

        expect(mockNotify).toHaveBeenCalledTimes(0);
    });

    it("notifyIfErrors should call notify if request status is Error and errorMessages is not empty", () => {
        const mockNotify = jest.fn();
        const errorMessages: ErrorMessage[] = [
            {
                message: "bar",
            },
            {
                message: "hello",
            },
            {
                message: "world",
            },
        ];
        notifyIfErrors(ActionStatus.Error, errorMessages, mockNotify, "foo");

        expect(mockNotify).toHaveBeenCalledTimes(3);
    });

    it("notifyIfErrors should call notify if with fallback msg", () => {
        const mockNotify = jest.fn();
        notifyIfErrors(ActionStatus.Error, null, mockNotify, "foo");

        expect(mockNotify).toHaveBeenCalledWith(
            NotificationTypeV1.Error,
            "foo"
        );
    });
});
