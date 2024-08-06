package net.quepierts.simpleanimator.core.config;

import com.google.common.io.Files;
import net.quepierts.simpleanimator.core.SimpleAnimator;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

// WIP
public class ClientConfiguration {
    private static final File FILE = new File("config", "simpleanimator-client.json");

    ClientConfiguration() {}

    public static ClientConfiguration load() {
        if (FILE.exists()) {
            try (Reader reader = Files.newReader(FILE, StandardCharsets.UTF_8)) {
                return SimpleAnimator.GSON.fromJson(reader, ClientConfiguration.class);
            } catch (IOException e) {
                SimpleAnimator.LOGGER.warn("Error occurred during loading common configuration!");
            }
        }
        ClientConfiguration configuration = new ClientConfiguration();
        configuration.write();
        return configuration;
    }

    public void write() {
        FILE.deleteOnExit();
        try (Writer writer = Files.newWriter(FILE, StandardCharsets.UTF_8)) {
            writer.write(SimpleAnimator.GSON.toJson(this));
        } catch (IOException e) {
            SimpleAnimator.LOGGER.warn("Error occurred during writing common configuration!");
        }
    }

    // TODO: 2024/8/4 May be enable bend model?
    public boolean hasBendModel() {
        return false;
    }
}
