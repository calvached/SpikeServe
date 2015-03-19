package echo;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EchoServer {
    public static void main(String[] args) {
        try {
            ServerSocket sock = new ServerSocket(5000);
            ArrayList<Hashtable<String, String>> savedData = new ArrayList<Hashtable<String, String>>(); // don't save to memory, instead use sessions or cookies

            while (true) {
                System.out.println("LISTENING...");
                Socket incoming = sock.accept(); // starts listening for a client socket

                BufferedReader in
                        = new BufferedReader(
                            new InputStreamReader(
                                incoming.getInputStream()));

                PrintWriter out
                        = new PrintWriter(
                            new OutputStreamWriter(
                                incoming.getOutputStream()));

                String request = in.readLine();

                if (request.contains("GET /form")) {
                    showData(savedData, out);

                    out.flush();
                    out.write("HTTP/1.1 200 OK\r\n");
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
                else if (request.contains("GET / HTTP/1.1")) {
                    System.out.println(request);
                    out.flush();
                    out.write("HTTP/1.1 200 OK\r\n");
                    out.flush();
                }
                else {
                    out.flush();
                    out.write("HTTP/1.1 404 Not Found\r\n");
                    out.flush();
                }

                incoming.close(); // stops listening
            }
        } catch (Exception err) {
            System.out.println(err);
        }
    }

    private static void showData(ArrayList<Hashtable<String, String>> savedData, PrintWriter out) {
        System.out.println(savedData);
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
        System.out.println(body);
        String [] lines = createLinesFrom(body);

        return createHashFrom(lines);
    }

    private static Hashtable createHashFrom(String[] lines) {
        Hashtable<String, String> postedData = new Hashtable<String, String>();
        Pattern keyValuePattern = Pattern.compile("\"?(\\w+)\"?=\"?(\\w+)\"?");

        for(String line : lines){
            Matcher lineMatcher = keyValuePattern.matcher(line.trim());
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
