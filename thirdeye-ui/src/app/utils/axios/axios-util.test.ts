import { AxiosError } from "axios";
import {
    getFulfilledResponseInterceptor,
    getRejectedResponseInterceptor,
    getRequestInterceptor,
} from "./axios-util";

const mockUnauthenticatedAccessHandler = jest.fn();

describe("Axios Util", () => {
    beforeEach(() => {
        jest.clearAllMocks();
    });

    afterAll(() => {
        jest.restoreAllMocks();
    });

    test("getRequestInterceptor shall return an axios request interceptor", () => {
        const requestInterceptor = getRequestInterceptor("testToken");

        expect(requestInterceptor).toBeInstanceOf(Function);
    });

    test("axios request interceptor shall attach valid access token to request header", () => {
        const requestInterceptor = getRequestInterceptor("testToken");
        const requestConfig = requestInterceptor({});

        expect(requestConfig.headers).toEqual({
            Authorization: "Bearer testToken",
        });
    });

    test("axios request interceptor shall not attach invalid access token to request header", () => {
        const requestInterceptor = getRequestInterceptor("");
        const requestConfig = requestInterceptor({});

        expect(requestConfig.headers).toBeUndefined();
    });

    test("getFulfilledResponseInterceptor shall return an axios response interceptor", () => {
        const responseInterceptor = getFulfilledResponseInterceptor();

        expect(responseInterceptor).toBeInstanceOf(Function);
    });

    test("axios fulfilled response interceptor shall return response as it is", () => {
        const responseInterceptor = getFulfilledResponseInterceptor();
        const response = responseInterceptor("testResponse");

        expect(response).toEqual(response);
    });

    test("getRejectedResponseInterceptor shall return an axios rejected response interceptor", () => {
        const responseInterceptor = getRejectedResponseInterceptor(
            mockUnauthenticatedAccessHandler
        );

        expect(responseInterceptor).toBeInstanceOf(Function);
    });

    test("axios rejected response interceptor shall invoke unauthenticatedAccessHandler and return 401 error", () => {
        const mockError = {
            response: {
                status: 401,
            },
        } as AxiosError;

        const responseInterceptor = getRejectedResponseInterceptor(
            mockUnauthenticatedAccessHandler
        );
        const error = responseInterceptor(mockError);

        expect(mockUnauthenticatedAccessHandler).toHaveBeenCalled();
        expect(error).toEqual(mockError);
    });

    test("axios rejected response interceptor shall not invoke unauthenticatedAccessHandler and return any error other than 401", () => {
        const mockError = {
            response: {
                status: 500,
            },
        } as AxiosError;

        const responseInterceptor = getRejectedResponseInterceptor(
            mockUnauthenticatedAccessHandler
        );
        const error = responseInterceptor(mockError);

        expect(mockUnauthenticatedAccessHandler).not.toHaveBeenCalled();
        expect(error).toEqual(mockError);
    });

    test("axios rejected response interceptor shall not invoke unauthenticatedAccessHandler and return any unknown error", () => {
        const mockError = {} as AxiosError;

        const responseInterceptor = getRejectedResponseInterceptor(
            mockUnauthenticatedAccessHandler
        );
        const error = responseInterceptor(mockError);

        expect(mockUnauthenticatedAccessHandler).not.toHaveBeenCalled();
        expect(error).toEqual(mockError);
    });
});
