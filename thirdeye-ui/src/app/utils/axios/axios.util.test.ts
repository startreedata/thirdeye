import { AxiosError } from "axios";
import {
    getFulfilledResponseInterceptor,
    getRejectedResponseInterceptor,
    getRequestInterceptor,
} from "./axios.util";

describe("Axios Util", () => {
    test("getRequestInterceptor should return axios request interceptor", () => {
        expect(getRequestInterceptor("testToken")).toBeInstanceOf(Function);
    });

    test("axios request interceptor should not attach invalid access token to request header", () => {
        const requestInterceptor = getRequestInterceptor(
            (null as unknown) as string
        );

        expect(requestInterceptor({})).toEqual({});
    });

    test("axios request interceptor should not attach empty access token to request header", () => {
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

    test("getFulfilledResponseInterceptor should return axios response interceptor", () => {
        expect(getFulfilledResponseInterceptor()).toBeInstanceOf(Function);
    });

    test("axios fulfilled response interceptor should return response as is", () => {
        const responseInterceptor = getFulfilledResponseInterceptor();

        expect(responseInterceptor("testResponse")).toEqual("testResponse");
    });

    test("getRejectedResponseInterceptor should return axios rejected response interceptor", () => {
        expect(
            getRejectedResponseInterceptor(mockHandleUnauthenticatedAccess)
        ).toBeInstanceOf(Function);
    });

    test("axios rejected response interceptor should throw 401 error and invoke unauthenticated access handler", () => {
        const mockError = {
            response: {
                status: 401,
            },
        } as AxiosError;
        const responseInterceptor = getRejectedResponseInterceptor(
            mockHandleUnauthenticatedAccess
        );

        expect(() => responseInterceptor(mockError)).toThrow();
        expect(mockHandleUnauthenticatedAccess).toHaveBeenCalled();
    });

    test("axios rejected response interceptor should throw any error other than 401 but not invoke unauthenticated access handler", () => {
        const mockError = {
            response: {
                status: 500,
            },
        } as AxiosError;
        const responseInterceptor = getRejectedResponseInterceptor(
            mockHandleUnauthenticatedAccess
        );

        expect(() => responseInterceptor(mockError)).toThrow();
        expect(mockHandleUnauthenticatedAccess).not.toHaveBeenCalled();
    });
});

const mockHandleUnauthenticatedAccess = jest.fn();
