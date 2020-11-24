import { AxiosError } from "axios";
import {
    getFulfilledResponseInterceptor,
    getRejectedResponseInterceptor,
    getRequestInterceptor,
} from "./axios-util";

describe("Axios Util", () => {
    test("getRequestInterceptor shall return an axios request interceptor", () => {
        const requestInterceptor = getRequestInterceptor("testToken");

        expect(requestInterceptor).toBeInstanceOf(Function);
    });

    test("axios request interceptor shall attach  valid access token to request header", () => {
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

    test("axios fulfill response interceptor shall return response interceptor", () => {
        const responseInterceptor = getFulfilledResponseInterceptor();

        expect(responseInterceptor).toBeInstanceOf(Function);
    });

    test("axios fulfill response interceptor shall return response as it's recieved", () => {
        const responseInterceptor = getFulfilledResponseInterceptor();
        const response = "Test";
        const interceptedResponse = responseInterceptor(response);

        expect(interceptedResponse).toBe(response);
    });

    test("axios reject response interceptor shall return response interceptor", () => {
        const responseInterceptor = getRejectedResponseInterceptor(jest.fn());

        expect(responseInterceptor).toBeInstanceOf(Function);
    });

    test("axios reject response interceptor shall call handler when response status is 401", () => {
        const mockFn = jest.fn();
        const responseInterceptor = getRejectedResponseInterceptor(mockFn);
        responseInterceptor({ response: { status: 401 } } as AxiosError);

        expect(mockFn).toHaveBeenCalled();
    });

    test("axios reject response interceptor shall not call handler when response status is not 401", () => {
        const mockFn = jest.fn();
        const responseInterceptor = getRejectedResponseInterceptor(mockFn);
        responseInterceptor({ response: { status: 400 } } as AxiosError);

        expect(mockFn).not.toHaveBeenCalled();
    });
});
