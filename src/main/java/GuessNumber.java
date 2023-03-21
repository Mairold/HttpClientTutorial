import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;

public class GuessNumber {

    public static void main(String[] args) throws IOException, InterruptedException {

        Scanner scanner = new Scanner(System.in);
        HttpClient httpClient = HttpClient.newHttpClient();
        if (startGame(httpClient)) {
            System.out.println("Hello! I've generated a random number between 1 and 100. Please guess what?");
        } else {
            System.out.println("Game is already running, can not start a new one! || Server is down!");
        }
        new GuessNumber().userGuess(scanner, httpClient);


        System.out.println("You have finished the game. Bye!");
    }

    private void userGuess(Scanner scanner, HttpClient httpClient) throws IOException, InterruptedException {
        while (true) {
            String input = scanner.next();
            if ( input.equals("exit")) {
                endGame(httpClient);
                return;
            }
            HttpResponse<String> httpResponse = checkGuess(httpClient, input);
            if (responseControl(httpResponse)) {
                if (endGame(httpClient)) {
                    return;
                } else {
                    System.out.println("The game is not active");
                }
            }
        }
    }

    private boolean endGame(HttpClient httpClient) throws IOException, InterruptedException {
        URI HTTP_SERVER_URI = URI.create("http://10.10.10.156:6666/end-game");
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(HTTP_SERVER_URI)
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.statusCode() == 200;
    }

    private HttpResponse<String> checkGuess(HttpClient httpClient, String userGuess) throws IOException, InterruptedException {
        URI HTTP_SERVER_URI = URI.create("http://10.10.10.156:6666/guess");
        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofString(userGuess);
        HttpRequest request = HttpRequest.newBuilder()
                .POST(bodyPublisher)
                .uri(HTTP_SERVER_URI)
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private static boolean startGame(HttpClient httpClient) throws IOException, InterruptedException {
        URI HTTP_SERVER_URI = URI.create("http://10.10.10.156:6666/start-game");
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(HTTP_SERVER_URI)
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.statusCode() == 200;
    }

    private boolean responseControl(HttpResponse response) {
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


