package com.university.research;

import com.university.research.repository.FileResearchRepository;
import com.university.research.repository.ResearchRepository;
import com.university.research.web.ResearchWebServer;

import java.nio.file.Path;

public class Main {
    public static void main(String[] args) throws Exception {
        int port = parsePort(System.getenv().getOrDefault("PORT", "8080"));
        Path dataFile = Path.of(System.getenv().getOrDefault("DATA_FILE", "data/research-data.bin"));
        Path publicDir = Path.of(System.getenv().getOrDefault("PUBLIC_DIR", "public"));

        ResearchRepository repository = new FileResearchRepository(dataFile);
        ResearchWebServer server = new ResearchWebServer(port, repository, publicDir);
        server.start();
        System.out.println("University Research Team Formation System is running on http://0.0.0.0:" + port);
    }

    private static int parsePort(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 8080;
        }
    }
}
