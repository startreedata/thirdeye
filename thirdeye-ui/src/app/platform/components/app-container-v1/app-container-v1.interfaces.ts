// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
import { ReactNode } from "react";

export interface AppContainerV1Props {
    name: string; // Application name, will show up in document title
    className?: string;
    children?: ReactNode;
}

export interface AppContainerV1ContextProps {
    name: string;
}
