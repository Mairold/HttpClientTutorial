import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;

public class GuessNumber {
    private final static String baseUri = "http://10.10.10.156:6666/";
    private static final HttpClient httpClient = HttpClient.newHttpClient();

    public static void main(String[] args) throws IOException, InterruptedException {
        Scanner scanner = new Scanner(System.in);

        startGame();
        askAndCheckInput(scanner);

        System.out.println("You have finished the game. Bye!");
    }

    private static void startGame() throws IOException, InterruptedException {
        HttpResponse<String> response = sendRequest("start-game", "");
        statusCodeCheck(response, "Hello! I've generated a random number between 1 and 100. Please guess what?");
    }

    private static void statusCodeCheck(HttpResponse<String> response, String message) {
        switch (response.statusCode()) {
            case 200 -> System.out.println(message);
            case 400 -> System.out.println("Error message: " + response.body());
            case 500 -> System.out.println("Server is down, please try again later");
        }
    }

    private static void askAndCheckInput(Scanner scanner) throws IOException, InterruptedException {
        while (true) {
            String input = scanner.next();

            if (input.equals("exit")) {
                HttpResponse<String> response = sendRequest("end-game", "");
                statusCodeCheck(response, "");
                return;
            }

            HttpResponse<String> httpResponse = sendRequest("guess", input);

            if (responseControl(httpResponse)) {
                HttpResponse<String> response = sendRequest("end-game", "");
                statusCodeCheck(response, "");
                return;
            }
        }
    }

    private static HttpResponse<String> sendRequest(String uri, String bodyMessage) throws IOException, InterruptedException {
        URI HTTP_SERVER_URI = URI.create(baseUri + uri);
        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofString(bodyMessage);
        HttpRequest request = HttpRequest.newBuilder()
                .POST(bodyPublisher)
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
            System.out.println("Error message: " + response.body() + " Please try again!");
        }
        return false;
    }
}


