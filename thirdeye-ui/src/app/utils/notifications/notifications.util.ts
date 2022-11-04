// Copyright 2022 StarTree Inc

// Licensed under the StarTree Community License (the "License"); you may not use
// this file except in compliance with the License. You may obtain a copy of the
// License at http://www.startree.ai/legal/startree-community-license

// Unless required by applicable law or agreed to in writing, software distributed under the
// License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
// either express or implied.
// See the License for the specific language governing permissions and limitations under
// the License.
import { isEmpty } from "lodash";
import { NotificationTypeV1 } from "../../platform/components";
import { ActionStatus } from "../../rest/actions.interfaces";

export const notifyIfErrors = (
    requestStatus: ActionStatus,
    errorMessages: string[] | null | undefined,
    notify: (msgType: NotificationTypeV1, msg: string) => void,
    fallbackMsg: string
): void => {
    if (requestStatus !== ActionStatus.Error) {
        return;
    }

    if (!isEmpty(errorMessages)) {
        errorMessages?.map((msg) => notify(NotificationTypeV1.Error, msg));
    } else {
        notify(NotificationTypeV1.Error, fallbackMsg);
    }
};
