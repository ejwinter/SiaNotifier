package edu.mayo.cim.sianotifier;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.SortedSet;
import java.util.TreeSet;

@Repository
public class NotificationDao implements AutoCloseable{

    private final Path daoFilePath;

    private final SortedSet<String> notified;


    public NotificationDao(@Value("${archiveNotification.notificationRepository}") String daoFilePath) throws IOException {
        this.daoFilePath = Paths.get(daoFilePath);
        this.notified = new TreeSet<>(Files.readAllLines(this.daoFilePath, StandardCharsets.UTF_8));
    }

    public boolean hasBeenNotified(String ngsNumber, String mnemonic){
        String toAdd = ngsNumber;
        if(mnemonic != null){
            toAdd = toAdd + "-" + mnemonic;
        }
        return this.notified.contains(toAdd);
    }

    public boolean registerNotification(String ngsNumber, String mnemonic){
        String toAdd = ngsNumber;
        if(mnemonic != null){
            toAdd = toAdd + "-" + mnemonic;
        }
        return this.notified.add(toAdd);
    }

    @Override
    public void close() throws Exception {
        try(BufferedWriter writer = Files.newBufferedWriter(daoFilePath, StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING)){
            writer.write(String.join("\n", notified));
        }
    }
}
