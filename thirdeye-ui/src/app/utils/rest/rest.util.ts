// /
// / Copyright 2022 StarTree Inc
// /
// / Licensed under the StarTree Community License (the "License"); you may not use
// / this file except in compliance with the License. You may obtain a copy of the
// / License at http://www.startree.ai/legal/startree-community-license
// /
// / Unless required by applicable law or agreed to in writing, software distributed under the
// / License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
// / either express or implied.
// / See the License for the specific language governing permissions and limitations under
// / the License.
// /

import { AxiosError } from "axios";
import { get, isEmpty } from "lodash";

export const getErrorMessages = (error: AxiosError): string[] => {
    const errMsgs: string[] = [];

    const errList = get(error, "response.data.list", []);

    if (Array.isArray(errList)) {
        errList.map((err: { code: string; msg: string }) => {
            // Toast error message
            if (!isEmpty(err.msg)) {
                errMsgs.push(err.msg);
            }
        });
    }

    return errMsgs;
};
