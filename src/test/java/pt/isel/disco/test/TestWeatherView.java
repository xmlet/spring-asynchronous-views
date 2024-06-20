package pt.isel.disco.test;

import org.junit.jupiter.api.Test;

import static pt.isel.disco.test.TestArtistView.assertHtml;
import static pt.isel.disco.test.TestArtistView.request;

public class TestWeatherView {

    @Test
    public void testHtmlFlowWeatherBlocking() {
        final var html = request("/htmlflow/blocking/weather/portugal");
        assertHtml(expectedWellFormed(), html);
    }

    @Test
    public void testHtmlFlowWeatherReactiveMalformed() {
        final var html = request("/htmlflow/reactive/weather/portugal");
        assertHtml(expectedMalformed(), html);
    }

    private static String expectedWellFormed() {
        return """
<!DOCTYPE html>
<html>
	<head>
		<title>
			Portugal
		</title>
	</head>
	<body>
		<table border="1">
			<tr>
				<th>
					City
				</th>
				<th>
					Temperature
				</th>
			</tr>
			<tr>
				<td>
					Porto
				</td>
				<td>
					14
				</td>
			</tr>
			<tr>
				<td>
					Lisbon
				</td>
				<td>
					14
				</td>
			</tr>
			<tr>
				<td>
					Sagres
				</td>
				<td>
					18
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
			Portugal
		</title>
	</head>
	<body>
		<table border="1">
			<tr>
				<th>
					City
				</th>
				<th>
					Temperature
				</th>
			</tr>
		</table>
	</body>
</html>
		<tr>
			<td>
				Porto
			</td>
			<td>
				14
			</td>
		</tr>
		<tr>
			<td>
				Lisbon
			</td>
			<td>
				14
			</td>
		</tr>
		<tr>
			<td>
				Sagres
			</td>
			<td>
				18
			</td>
		</tr>
                """;
    }
}
