package pt.isel.disco.test;

import org.junit.jupiter.api.Test;

import static pt.isel.disco.test.TestArtistView.assertHtml;
import static pt.isel.disco.test.TestArtistView.request;

public class TestWeatherView {

    @Test
    public void testHtmlFlowWeatherBlocking() {
        final var html = request("/htmlflow/blocking/weather/australia");
        assertHtml(expectedWellFormed(), html);
    }
    @Test
    public void testHtmlFlowWeatherAsync() {
        final var html = request("/htmlflow/async/weather/australia");
        assertHtml(expectedWellFormed(), html);
    }

    @Test
    public void testHtmlFlowWeatherSuspending() {
        final var html = request("/htmlflow/suspending/weather/australia");
        assertHtml(expectedWellFormed(), html);
    }

    @Test
    public void testHtmlFlowWeatherReactiveMalformed() {
        final var html = request("/htmlflow/reactive/weather/australia");
        assertHtml(expectedMalformed(), html);
    }

    private static String expectedWellFormed() {
        return """
<!DOCTYPE html>
<html>
	<head>
		<title>
			Australia
		</title>
	</head>
	<body>
		<table border="1">
			<tr>
				<th>
					City
				</th>
				<th>
					Celsius
				</th>
			</tr>
			<tr>
				<td>
					Adelaide
				</td>
				<td>
					9
				</td>
			</tr>
			<tr>
				<td>
					Darwin
				</td>
				<td>
					31
				</td>
			</tr>
			<tr>
				<td>
					Perth
				</td>
				<td>
					16
				</td>
			</tr>
		</table>
	</body>
</html>
        """;
    }

    private static String expectedMalformed() {
        return """
<!DOCTYPE html>
<html>
	<head>
		<title>
			Australia
		</title>
	</head>
	<body>
		<table border="1">
			<tr>
				<th>
					City
				</th>
				<th>
					Celsius
				</th>
			</tr>
		</table>
	</body>
</html>
		<tr>
			<td>
				Adelaide
			</td>
			<td>
				9
			</td>
		</tr>
		<tr>
			<td>
				Darwin
			</td>
			<td>
				31
			</td>
		</tr>
		<tr>
			<td>
				Perth
			</td>
			<td>
				16
			</td>
		</tr>
                """;
    }
}
