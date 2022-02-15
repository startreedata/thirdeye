// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
export interface NavBarV1 {
    navBarMinimized: boolean;
    navBarUserPreference: number;
    minimizeNavBar: () => void;
    maximizeNavBar: () => void;
    setNavBarUserPreference: (userPreference: number) => void;
}
