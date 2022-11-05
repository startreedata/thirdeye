export interface NavBarV1 {
    navBarMinimized: boolean;
    navBarUserPreference: number;
    minimizeNavBar: () => void;
    maximizeNavBar: () => void;
    setNavBarUserPreference: (userPreference: number) => void;
}
