// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
import { AxiosError } from "axios";
import {
    getFulfilledResponseInterceptorV1,
    getRejectedResponseInterceptorV1,
    getRequestInterceptorV1,
} from "./axios.util";

describe("Axios Util", () => {
    it("getRequestInterceptorV1 should return axios request interceptor", () => {
        expect(getRequestInterceptorV1("testToken")).toBeInstanceOf(Function);
    });

    it("axios request interceptor should not attach invalid access token to request header", () => {
        const requestInterceptor = getRequestInterceptorV1(
            (null as unknown) as string
        );

        expect(requestInterceptor({})).toEqual({});
    });

    it("axios request interceptor should not attach empty access token to request header", () => {
        const requestInterceptor = getRequestInterceptorV1("");

        expect(requestInterceptor({})).toEqual({});
    });

    it("axios request interceptor should attach access token to request header", () => {
        const requestInterceptor = getRequestInterceptorV1("testToken");

        expect(requestInterceptor({})).toEqual({
            headers: {
                Authorization: "Bearer testToken",
            },
        });
    });

    it("getFulfilledResponseInterceptorV1 should return axios response interceptor", () => {
        expect(getFulfilledResponseInterceptorV1()).toBeInstanceOf(Function);
    });

    it("axios fulfilled response interceptor should return response as is", () => {
        const responseInterceptor = getFulfilledResponseInterceptorV1();

        expect(responseInterceptor("testResponse")).toEqual("testResponse");
    });

    it("getRejectedResponseInterceptorV1 should return axios rejected response interceptor", () => {
        expect(
            getRejectedResponseInterceptorV1(mockHandleUnauthenticatedAccess)
        ).toBeInstanceOf(Function);
    });

    it("axios rejected response interceptor should throw 401 error and invoke unauthenticated access function", () => {
        const responseInterceptor = getRejectedResponseInterceptorV1(
            mockHandleUnauthenticatedAccess
        );

        expect(() =>
            responseInterceptor(mockUnauthenticatedAccessError)
        ).toThrow();
        expect(mockHandleUnauthenticatedAccess).toHaveBeenCalled();
    });

    it("axios rejected response interceptor should throw any error other than 401 and not invoke unauthenticated access function", () => {
        const responseInterceptor = getRejectedResponseInterceptorV1(
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
