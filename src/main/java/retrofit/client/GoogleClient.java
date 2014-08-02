/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package retrofit.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import retrofit.mime.TypedInput;
import retrofit.mime.TypedOutput;

import com.google.api.client.http.AbstractHttpContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.util.Preconditions;

public class GoogleClient implements Client
{
	private final HttpRequestFactory httpRequestFactory;

	public GoogleClient(HttpRequestFactory httpRequestFactory)
	{
		this.httpRequestFactory = Preconditions.checkNotNull(httpRequestFactory, "HttpRequestFactory is required");
	}

	@Override
	public Response execute(Request request) throws IOException
	{
		HttpRequest httpRequest = createHttpRequest(request);
		HttpResponse httpResponse = httpRequest.execute();
		Response response = readResponse(httpResponse);
		return response;
	}

	private HttpRequest createHttpRequest(Request request) throws IOException
	{
		String method = Preconditions.checkNotNull(request.getMethod(), "HTTP Method is required");
		String url = Preconditions.checkNotNull(request.getUrl(), "URL is required");
		HttpContent content = request.getBody() == null ? null : new TypedHttpContent(request.getBody());

		HttpRequest httpRequest =
		    httpRequestFactory.buildRequest(
		        method,
		        new GenericUrl(url),
		        content);

		HttpHeaders httpHeaders = new HttpHeaders();
		for (Header header : request.getHeaders())
		{
			httpHeaders.set(header.getName(), header.getValue());
		}
		httpRequest.setHeaders(httpHeaders);

		return httpRequest;
	}

	private Response readResponse(HttpResponse httpResponse) throws IOException
	{
		int status = httpResponse.getStatusCode();
		String reason = httpResponse.getStatusMessage() == null ? "" : httpResponse.getStatusMessage();
		int length = -1;
		String url = httpResponse.getRequest().getUrl().toString();
		String mimeType = httpResponse.getContentType();
		InputStream stream = httpResponse.getContent();
		TypedInput responseBody = new TypedInputStream(mimeType, length, stream);

		List<Header> headers = new ArrayList<Header>();
		for (Entry<String, Object> field : httpResponse.getHeaders().entrySet())
		{
			String name = field.getKey();
			headers.add(new Header(name, field.getValue().toString()));
		}

		return new Response(url, status, reason, headers, responseBody);
	}

	private static class TypedInputStream implements TypedInput
	{
		private final String mimeType;
		private final long length;
		private final InputStream stream;

		private TypedInputStream(String mimeType, long length, InputStream stream)
		{
			this.mimeType = mimeType;
			this.length = length;
			this.stream = stream;
		}

		@Override
		public String mimeType()
		{
			return mimeType;
		}

		@Override
		public long length()
		{
			return length;
		}

		@Override
		public InputStream in() throws IOException
		{
			return stream;
		}
	}

	private static class TypedHttpContent extends AbstractHttpContent
	{
		private final TypedOutput typedOutput;

		public TypedHttpContent(TypedOutput typedOutput)
		{
			super(typedOutput.mimeType());
			this.typedOutput = typedOutput;
		}

		@Override
		public void writeTo(OutputStream out) throws IOException
		{
			typedOutput.writeTo(out);
		}
	}
}
