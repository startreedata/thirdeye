export interface AlertFetchRedirectParamsProps {
    to: string;
    replace?: boolean;
    fallbackDurationGenerator: () => [number, number];
}
