// Copyright 2022 StarTree Inc

// Licensed under the StarTree Community License (the "License"); you may not use
// this file except in compliance with the License. You may obtain a copy of the
// License at http://www.startree.ai/legal/startree-community-license

// Unless required by applicable law or agreed to in writing, software distributed under the
// License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
// either express or implied.
// See the License for the specific language governing permissions and limitations under
// the License.
import { AuthExceptionCodeV1 } from "../../components/auth-provider-v1/auth-provider-v1.interfaces";
import { isBlockingAuthExceptionV1 } from "./auth.util";

describe("Auth Util", () => {
    it("isBlockingAuthExceptionV1 should return false for invalid auth exception code", () => {
        expect(
            isBlockingAuthExceptionV1(null as unknown as AuthExceptionCodeV1)
        ).toBeFalsy();
    });

    it("isBlockingAuthExceptionV1 should return false for AuthExceptionCodeV1.UnauthorizedAccess", () => {
        expect(
            isBlockingAuthExceptionV1(AuthExceptionCodeV1.UnauthorizedAccess)
        ).toBeFalsy();
    });

    it("isBlockingAuthExceptionV1 should return true for any exception code other than AuthExceptionCodeV1.UnauthorizedAccess", () => {
        expect(
            isBlockingAuthExceptionV1(AuthExceptionCodeV1.InitFailure)
        ).toBeTruthy();
        expect(
            isBlockingAuthExceptionV1(AuthExceptionCodeV1.InfoCallFailure)
        ).toBeTruthy();
        expect(
            isBlockingAuthExceptionV1(
                AuthExceptionCodeV1.OpenIDConfigurationCallFailure
            )
        ).toBeTruthy();
        expect(
            isBlockingAuthExceptionV1(AuthExceptionCodeV1.InfoMissing)
        ).toBeTruthy();
        expect(
            isBlockingAuthExceptionV1(
                AuthExceptionCodeV1.OpenIDConfigurationMissing
            )
        ).toBeTruthy();
    });
});
