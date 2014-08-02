package retrofit.client;

import java.util.List;

import retrofit.RestAdapter;
import retrofit.http.GET;
import retrofit.http.Path;

import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.javanet.NetHttpTransport;

public class GitHubClientExample
{

	public static void main(String[] args)
	{
		NetHttpTransport transport = new NetHttpTransport();
		HttpRequestFactory requestFactory = transport.createRequestFactory();
		GoogleClient client = new GoogleClient(requestFactory);

		RestAdapter restAdapter =
		    new RestAdapter.Builder()
		        .setEndpoint("https://api.github.com")
		        .setClient(client)
		        .build();

		GitHub gitHub = restAdapter.create(GitHub.class);
		List<Contributor> contributors = gitHub.contributors("square", "retrofit");
		System.out.println(contributors);
	}

	static class Contributor
	{
		String login;
		int contributions;
	}

	interface GitHub
	{
		@GET("/repos/{owner}/{repo}/contributors")
		List<Contributor> contributors(@Path("owner") String owner, @Path("repo") String repo);
	}
	
}
