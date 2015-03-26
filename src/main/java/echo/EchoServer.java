package echo;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EchoServer {
    public static void main(String[] args) { // avoid static methods (harder to test)
        try {
            ServerSocket sock = new ServerSocket(5000);

            // use a session to save data
            ArrayList<Hashtable<String, String>> savedData = new ArrayList<Hashtable<String, String>>();

            while (true) {
                // catch should be for catching an error, but keep the server running
                System.out.println("LISTENING...");
                Socket incoming = sock.accept();

                BufferedReader in
                      = new BufferedReader(
                            new InputStreamReader(
                                incoming.getInputStream()));

                OutputStream outStream = incoming.getOutputStream();

                // don't need PrintWriter
                PrintWriter out
                      = new PrintWriter(
                            new OutputStreamWriter(
                                outStream));

                // IF in.ready() is true then read the line
                // ELSE close the connection

                String request = in.readLine();

                System.out.println(request);

                if (request.contains("GET /form")) {
                    showData(savedData, out);

                    out.flush();
                    out.write("HTTP/1.1 200 OK");
                    out.write("Content-Type: text/plain");
                    out.write("\r\n");
                    out.write("some response here");
                    out.flush();
                }
                else if (request.contains("POST /form")) {
                    Hashtable postData = parseData(in);
                    save(savedData, postData);

                    out.flush();
                    out.write("HTTP/1.1 200 OK\r\n");
                    out.flush();
                }
                else if (request.contains("PUT /form")) {
                    Hashtable putData = parseData(in);
                    update(savedData, putData);

                    out.flush();
                    out.write("HTTP/1.1 200 OK\r\n");
                    out.flush();
                }
                else if (request.contains("DELETE /form")) {
                    Hashtable deleteData = parseData(in);
                    delete(savedData, deleteData);

                    out.flush();
                    out.write("HTTP/1.1 200 OK\r\n");
                    out.flush();
                }
                else if (request.contains("GET /sample.pdf")) {
                    byte[] bytes = convertToByteArray("public/sample.pdf");

                    out.flush();
                    outStream.write(bytes);
                    out.flush();
                }
                else if (request.contains("GET /mindblown.gif")) {
                    byte[] bytes = convertToByteArray("public/mindblown.gif");

                    out.flush();
                    // socket is closing before I can send/finish a request
                    outStream.write(bytes);
                    out.flush();
                }
                else if (request.contains("GET / HTTP/1.1")) {
                    out.flush();
                    out.write("HTTP/1.1 200 OK\r\n");
                    out.flush();
                }
                else {
                    out.flush();
                    out.write("HTTP/1.1 404 Not Found\r\n");
                    out.flush();
                }

                incoming.close();
            }
        } catch (Exception err) {
            System.out.println(err);
            err.printStackTrace();
        }
    }

    public static byte[] convertToByteArray(String file) throws IOException {
        // image specific bug? Maybe because it's a large image? 9M Multiple threads?
        Path path = Paths.get(file);
        byte[] data = Files.readAllBytes(path);

        return data;
    }

    private static void showData(ArrayList<Hashtable<String, String>> savedData, PrintWriter out) {
        out.println(savedData);
        out.flush();
    }

    private static void delete(ArrayList<Hashtable<String, String>> savedData, Hashtable deleteData) {
        List<Hashtable> copySavedData = new ArrayList<Hashtable>(savedData);

        for (Hashtable dataSet : copySavedData) {
            if (dataSet.get("id").equals(deleteData.get("id"))) {
                savedData.remove(dataSet);
            }
        }
    }

    private static void save(ArrayList<Hashtable<String, String>> savedData, Hashtable data) {
        savedData.add(data);
    }

    private static void update(ArrayList<Hashtable<String, String>> savedData, Hashtable putData) {
        // a PATCH will replace only portions of a record
        for (Hashtable dataSet : savedData) {
            if (dataSet.get("id").equals(putData.get("id"))) {
                savedData.remove(dataSet);
                save(savedData, putData);
            }
        }
    }

    private static Hashtable parseData(BufferedReader in) throws IOException {
        String request = requestToString(in);
        String body = captureBodyFrom(request);
        String [] lines = createLinesFrom(body);

        return createHashFrom(lines);
    }

    private static Hashtable createHashFrom(String[] lines) {
        Hashtable<String, String> postedData = new Hashtable<String, String>();
        Pattern findKeyValue = Pattern.compile("\"?(\\w+)\"?=\"?(\\w+)\"?");

        for(String line : lines){
            Matcher lineMatcher = findKeyValue.matcher(line.trim());
            lineMatcher.find();
            postedData.put(lineMatcher.group(1), lineMatcher.group(2));
        }

        return postedData;
    }

    private static String requestToString(BufferedReader in) throws IOException {
        String request = "";

        while(in.ready()) {
            int content = in.read();
            request+=((char) content);
        }

        return request;
    }

    private static String captureBodyFrom(String request) {
        // cob_spec does not post JSON, it's a simpler format, redo REGEX
        Pattern findContent = Pattern.compile("\\{([^}]*)\\}");
        Matcher requestMatcher = findContent.matcher(request);

        requestMatcher.find();

        return requestMatcher.group(1);
    }

    private static String[] createLinesFrom(String body) {
        Pattern atComma = Pattern.compile(",");
        String [] lines = atComma.split(body);

        return lines;
    }
}
