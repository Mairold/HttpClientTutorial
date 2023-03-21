import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;

public class GuessNumber {
    private final static String baseUri = "http://10.10.10.156:6666/";
    public static void main(String[] args) throws IOException, InterruptedException {

        Scanner scanner = new Scanner(System.in);
        HttpClient httpClient = HttpClient.newHttpClient();
        startGame(httpClient);
        askAndCheckInput(scanner, httpClient);
        System.out.println("You have finished the game. Bye!");
    }

    private static void startGame(HttpClient httpClient) throws IOException, InterruptedException {
        HttpResponse<String> response = game(httpClient, "start-game");
        switch (response.statusCode()) {
            case 200 -> System.out.println("Hello! I've generated a random number between 1 and 100. Please guess what?");
            case 400 -> System.out.println("Error message: " + response.body());
            case 500 -> System.out.println("Server is down, please try again later");
        }
    }

    private static void askAndCheckInput(Scanner scanner, HttpClient httpClient) throws IOException, InterruptedException {
        while (true) {
            String input = scanner.next();

            if ( input.equals("exit")) {
                HttpResponse<String> response = game(httpClient, "end-game");
                if (response.statusCode() == 400) {
                    System.out.println("Error message: " + response.body());
                } else {
                    return;
                }
            }

            HttpResponse<String> httpResponse = checkGuess(httpClient, input);

            if (responseControl(httpResponse)) {
                HttpResponse<String> response = game(httpClient, "end-game");
                if (response.statusCode() == 400) {
                    System.out.println("Error message: " + response.body());
                } else {
                    return;
                }
            }
        }
    }

    private static HttpResponse<String> checkGuess(HttpClient httpClient, String userGuess) throws IOException, InterruptedException {
        URI HTTP_SERVER_URI = URI.create("http://10.10.10.156:6666/guess");
        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofString(userGuess);
        HttpRequest request = HttpRequest.newBuilder()
                .POST(bodyPublisher)
                .uri(HTTP_SERVER_URI)
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private static HttpResponse<String> game(HttpClient httpClient, String uri) throws IOException, InterruptedException {
        URI HTTP_SERVER_URI = URI.create(baseUri + uri);
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(HTTP_SERVER_URI)
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private static boolean responseControl(HttpResponse response) {
        if (response.statusCode() == 200) {

            switch (response.body().toString()) {
                case "EQUAL" -> {
                    System.out.println("Wow! You are a genius! Correct answer!");
                    return true;
                }
                case "BIGGER" -> System.out.println("The number is bigger than you guessed. Please try again!");
                case "LESS" -> System.out.println("The number is smaller than you guessed. Please try again!");
            }
        } else {
            System.out.println(response.body().toString());
        }
        return false;
    }
}


