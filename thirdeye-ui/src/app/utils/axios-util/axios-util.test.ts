import { AxiosError } from "axios";
import {
    getFulfilledResponseInterceptor,
    getRejectedResponseInterceptor,
    getRequestInterceptor,
} from "./axios-util";

describe("Axios Util", () => {
    test("getRequestInterceptor should return an axios request interceptor", () => {
        expect(getRequestInterceptor("testToken")).toBeInstanceOf(Function);
    });

    test("axios request interceptor should not attach invalid access token to request header", () => {
        const requestInterceptor = getRequestInterceptor("");

        expect(requestInterceptor({})).toEqual({});
    });

    test("axios request interceptor should attach access token to request header", () => {
        const requestInterceptor = getRequestInterceptor("testToken");

        expect(requestInterceptor({})).toEqual({
            headers: {
                Authorization: "Bearer testToken",
            },
        });
    });

    test("getFulfilledResponseInterceptor should return an axios response interceptor", () => {
        expect(getFulfilledResponseInterceptor()).toBeInstanceOf(Function);
    });

    test("axios fulfilled response interceptor should return response as is", () => {
        const responseInterceptor = getFulfilledResponseInterceptor();

        expect(responseInterceptor("testResponse")).toEqual("testResponse");
    });

    test("getRejectedResponseInterceptor should return an axios rejected response interceptor", () => {
        expect(
            getRejectedResponseInterceptor(mockUnauthenticatedAccessHandler)
        ).toBeInstanceOf(Function);
    });

    test("axios rejected response interceptor should invoke unauthenticatedAccessHandler and throw 401 error", () => {
        const mockError = {
            response: {
                status: 401,
            },
        } as AxiosError;

        const responseInterceptor = getRejectedResponseInterceptor(
            mockUnauthenticatedAccessHandler
        );

        expect(() => responseInterceptor(mockError)).toThrow();
        expect(mockUnauthenticatedAccessHandler).toHaveBeenCalled();
    });

    test("axios rejected response interceptor should not invoke unauthenticatedAccessHandler and throw any error other than 401", () => {
        const mockError = {
            response: {
                status: 500,
            },
        } as AxiosError;

        const responseInterceptor = getRejectedResponseInterceptor(
            mockUnauthenticatedAccessHandler
        );

        expect(() => responseInterceptor(mockError)).toThrow();
        expect(mockUnauthenticatedAccessHandler).not.toHaveBeenCalled();
    });
});

const mockUnauthenticatedAccessHandler = jest.fn();
