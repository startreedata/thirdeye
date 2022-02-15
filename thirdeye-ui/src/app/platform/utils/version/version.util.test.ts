// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
import { getPlatformUiVersionV1 } from "./version.util";

describe("Version Util", () => {
    it("getPlatformUiVersionV1 should return appropriate version number placeholder", () => {
        expect(getPlatformUiVersionV1()).toEqual(
            "0.0.0-development-platform-ui"
        );
    });
});
