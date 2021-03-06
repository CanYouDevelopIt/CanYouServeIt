package impl;

/**
 * Interface for an HTTP service
 */
public interface IHttpService {

	/**
	 * Service serving point
	 * 
	 * @param request
	 *            The request to fulfill
	 * @param response
	 *            The response where to write
	 */
	void serve(IHttpRequest request, IHttpResponse response);
}