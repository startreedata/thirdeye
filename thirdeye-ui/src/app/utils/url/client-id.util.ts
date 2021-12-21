/**
 * The format of the url when deployed is expected to be:
 *
 * {deployment id}.{namespace}.{platform domain}:30000
 *
 * Client ID is expected to be: {namespace}-{deploymentId}
 *
 * http://thirdeye-tv592d4w.te.192.168.64.37.nip.io:30000 => te-thirdeye-tv592d4w
 *
 *
 * @example
 * getClientIdFromUrl(window.location.href)
 *
 * @example
 * getClientIdFromUrl("http://hello.world.com") => null
 *
 * @example
 * getClientIdFromUrl("http://localhost:1755") => null
 *
 * @example
 * getClientIdFromUrl("localhost:1755") => null
 *
 * @example
 * getClientIdFromUrl("http://thirdeye-tv592d4w.te.192.168.64.37.nip.io:30000")
 * => te-thirdeye-tv592d4w
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

    if (splitBySubDomain.length > 2) {
        return `${splitBySubDomain[1]}-${splitBySubDomain[0]}`;
    }

    return null;
};
