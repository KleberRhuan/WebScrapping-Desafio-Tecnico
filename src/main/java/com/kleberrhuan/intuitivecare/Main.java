package com.kleberrhuan.intuitivecare;

import com.kleberrhuan.intuitivecare.cli.CliManager;
import com.kleberrhuan.intuitivecare.util.helpers.ScannerHelper;
import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        LOGGER.info("Iniciando aplicação IntuitiveCare");
        try (ScannerHelper scannerHelper = new ScannerHelper()) {
            CliManager cliManager = new CliManager(scannerHelper);
            cliManager.run();
        } catch (Exception e) {
            LOGGER.error("Erro durante a execução do programa: {}", e.getMessage(), e);
        }

        LOGGER.info("Aplicação finalizada");
    }
}