import { debounce } from "lodash";
import { useEffect, useState } from "react";

export interface WindowSize {
    windowHeight: number;
    windowWidth: number;
}

export const useWindowSize = (): WindowSize => {
    const [windowSize, setWindowSize] = useState({
        windowHeight: window.innerHeight,
        windowWidth: window.innerWidth,
    });

    const handleResize = debounce((): void => {
        const { innerHeight, innerWidth } = window;
        setWindowSize({ windowHeight: innerHeight, windowWidth: innerWidth });
    }, 0);
    useEffect(() => {
        window.addEventListener("resize", handleResize);

        return () => window.removeEventListener("resize", handleResize);
    }, [setWindowSize]);

    return windowSize;
};
