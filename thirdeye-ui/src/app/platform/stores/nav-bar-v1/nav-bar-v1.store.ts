// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
import create from "zustand";
import { persist } from "zustand/middleware";
import { NavBarV1 } from "./nav-bar-v1.interfaces";

const KEY_NAV_BAR = "nav-bar-v1";

// App store for nav bar, persisted in browser local storage
export const useNavBarV1 = create<NavBarV1>(
    persist(
        (set) => ({
            navBarMinimized: false,
            navBarUserPreference: 0,

            minimizeNavBar: () => {
                set({
                    navBarMinimized: true,
                });
            },

            maximizeNavBar: () => {
                set({
                    navBarMinimized: false,
                });
            },

            setNavBarUserPreference: (userPreference) => {
                set({
                    navBarUserPreference: userPreference || 0,
                });
            },
        }),
        {
            name: KEY_NAV_BAR, // Persist in browser local storage
        }
    )
);
