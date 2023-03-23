import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;
import java.util.Scanner;
import java.util.SplittableRandom;

public class GuessNumber {
    private static String baseUri;
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static Integer sessionKey = 0;

    public static void main(String[] args) throws IOException, InterruptedException {
        baseUri = args.length > 0 ? args[0] : "http://10.10.10.156:6666/";
        Scanner scanner = new Scanner(System.in);
        boolean playAgain = true;
        do {
            startGame();
            askAndCheckInput(scanner);
            System.out.println("The game has ended! Do you wish to play again (Y,N)?");
            String input = scanner.next();
            if (showStatistics(input)) {
                System.out.println("Do you still wish to play again (Y,N)?");
                input = scanner.next();
            }
            if ("N".equalsIgnoreCase(input)) {
                playAgain = false;
                exit();
            }
        } while (playAgain);

        System.out.println("Thank you, come again!");
    }

    private static void startGame() throws IOException, InterruptedException {
        HttpResponse<String> response = postRequest("start-game", "");
        try {
            sessionKey = Integer.parseInt(response.body().split(":")[1]);
        } catch (NumberFormatException e) {
            System.out.println("Game has crashed on server, please restart!");
        }
        statusCodeCheck(response, "Hello! I've generated a random number between 1 and 100. Please guess what?");
    }

    private static void statusCodeCheck(HttpResponse<String> response, String message) {
        switch (response.statusCode()) {
            case 200 -> {
                if (!Objects.equals(message, "")) {
                    System.out.println(message);
                }
            }
            case 400 -> System.out.println("Error message: " + response.body());
            case 500 -> System.out.println("Server is down, please try again later");
        }
    }

    private static void askAndCheckInput(Scanner scanner) throws IOException, InterruptedException {
        while (true) {
            String input = scanner.next();

            if ("exit".equalsIgnoreCase(input)) return;
            if (showStatistics(input)) continue;

            HttpResponse<String> httpResponse = postRequest("guess", input);
            if (responseControl(httpResponse)) return;
        }
    }

    private static void exit() throws IOException, InterruptedException {
        HttpResponse<String> response = postRequest("end-game", "");
        statusCodeCheck(response, "");
    }

    private static boolean showStatistics(String input) throws IOException, InterruptedException {
        if (("stats").equals(input)) {
            HttpResponse<String> response = getRequest("stats");
            statusCodeCheck(response, response.body());
            return true;
        }
        return false;
    }

    private static HttpResponse<String> postRequest(String uri, String bodyMessage) throws IOException, InterruptedException {
        String finalUri = sessionKey.equals(null) ? baseUri + uri : baseUri + uri + "?session=" + sessionKey;
        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofString(bodyMessage);
        HttpRequest request = HttpRequest.newBuilder()
                .POST(bodyPublisher)
                .uri(URI.create(finalUri))
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private static HttpResponse<String> getRequest(String uri) throws IOException, InterruptedException {
        URI HTTP_SERVER_URI = URI.create(baseUri + uri + "?session=" + sessionKey);
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
                case "LESS" -> System.out.println("The number is bigger than you guessed. Please try again!");
                case "BIGGER" -> System.out.println("The number is smaller than you guessed. Please try again!");
            }
        } else {
            System.out.println("Error message: " + response.body() + " Please try again!");
        }
        return false;
    }
}


