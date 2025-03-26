package com.kleberrhuan.intuitivecare;

import com.kleberrhuan.intuitivecare.util.ScannerHelper;

public class Main {
    public static void main(String[] args) {
        try (ScannerHelper scannerHelper = new ScannerHelper()) {
            CliManager cliManager = new CliManager(scannerHelper);
            cliManager.run();
        }
    }
}
