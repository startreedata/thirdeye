// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
import { AuthExceptionCodeV1 } from "../../components/auth-provider-v1/auth-provider-v1.interfaces";
import { isBlockingAuthExceptionV1 } from "./auth.util";

describe("Auth Util", () => {
    it("isBlockingAuthExceptionV1 should return false for invalid auth exception code", () => {
        expect(
            isBlockingAuthExceptionV1((null as unknown) as AuthExceptionCodeV1)
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
