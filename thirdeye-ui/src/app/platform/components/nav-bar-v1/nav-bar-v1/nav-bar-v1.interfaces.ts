// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
import { ReactNode } from "react";

export interface NavBarV1Props {
    homePath?: string;
    minimizeLabel: string;
    maximizeLabel: string;
    className?: string;
    children?: ReactNode;
}

export interface NavBarV1ContextProps {
    navBarMinimized: boolean;
}

export enum NavBarUserPreferenceV1 {
    Minimized,
    Maximized,
}
