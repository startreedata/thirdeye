import { AxiosError } from "axios";
import {
    getFulfilledResponseInterceptor,
    getRejectedResponseInterceptor,
    getRequestInterceptor,
} from "./axios.util";

describe("Axios Util", () => {
    it("getRequestInterceptor should return axios request interceptor", () => {
        expect(getRequestInterceptor("testToken")).toBeInstanceOf(Function);
    });

    it("axios request interceptor should not attach invalid access token to request header", () => {
        const requestInterceptor = getRequestInterceptor(
            (null as unknown) as string
        );

        expect(requestInterceptor({})).toEqual({});
    });

    it("axios request interceptor should not attach empty access token to request header", () => {
        const requestInterceptor = getRequestInterceptor("");

        expect(requestInterceptor({})).toEqual({});
    });

    it("axios request interceptor should attach access token to request header", () => {
        const requestInterceptor = getRequestInterceptor("testToken");

        expect(requestInterceptor({})).toEqual({
            headers: {
                Authorization: "Bearer testToken",
            },
        });
    });

    it("getFulfilledResponseInterceptor should return axios response interceptor", () => {
        expect(getFulfilledResponseInterceptor()).toBeInstanceOf(Function);
    });

    it("axios fulfilled response interceptor should return response as is", () => {
        const responseInterceptor = getFulfilledResponseInterceptor();

        expect(responseInterceptor("testResponse")).toEqual("testResponse");
    });

    it("getRejectedResponseInterceptor should return axios rejected response interceptor", () => {
        expect(
            getRejectedResponseInterceptor(mockHandleUnauthenticatedAccess)
        ).toBeInstanceOf(Function);
    });

    it("axios rejected response interceptor should throw 401 error and invoke unauthenticated access function", () => {
        const responseInterceptor = getRejectedResponseInterceptor(
            mockHandleUnauthenticatedAccess
        );

        expect(() =>
            responseInterceptor(mockUnauthenticatedAccessError)
        ).toThrow();
        expect(mockHandleUnauthenticatedAccess).toHaveBeenCalled();
    });

    it("axios rejected response interceptor should throw any error other than 401 and not invoke unauthenticated access function", () => {
        const responseInterceptor = getRejectedResponseInterceptor(
            mockHandleUnauthenticatedAccess
        );

        expect(() => responseInterceptor(mockInternalServerError)).toThrow();
        expect(mockHandleUnauthenticatedAccess).not.toHaveBeenCalled();
    });
});

const mockHandleUnauthenticatedAccess = jest.fn();

const mockUnauthenticatedAccessError = {
    response: {
        status: 401,
    },
} as AxiosError;

const mockInternalServerError = {
    response: {
        status: 500,
    },
} as AxiosError;
