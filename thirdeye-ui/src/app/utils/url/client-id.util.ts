/**
 * The client id expected to be the first subdomain of the url passed i.e.
 * http://client-id.aws.startree-dev.startree.cloud
 *
 * @example
 * getClientIdFromUrl(window.location.href)
 *
 * @example
 * getClientIdFromUrl("http://hello.world.com") => "hello"
 *
 * @example
 * getClientIdFromUrl("http://localhost:1755") => "localhost:1755"
 *
 * @example
 * getClientIdFromUrl("localhost:1755") => null
 *
 * @param {string} url - Get the client id from this url
 */
export const getClientIdFromUrl = (url: string): string | null => {
    let host = "";

    try {
        const parsedUrl = new URL(url);

        if (parsedUrl.host === "") {
            return null;
        }

        host = parsedUrl.host;
    } catch (e) {
        return null;
    }

    const splitBySubDomain = host.split(".");

    return splitBySubDomain[0];
};
