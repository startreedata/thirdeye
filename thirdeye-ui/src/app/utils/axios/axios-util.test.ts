import { getRequestInterceptor } from "./axios-util";

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
});
