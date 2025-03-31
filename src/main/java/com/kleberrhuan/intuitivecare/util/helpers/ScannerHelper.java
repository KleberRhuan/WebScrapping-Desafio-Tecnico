package com.kleberrhuan.intuitivecare.util.helpers;

import java.util.Scanner;

/**
 * Classe auxiliar para lidar com entrada do usuário via console.
 * Implementa AutoCloseable para garantir o fechamento do Scanner.
 */
public class ScannerHelper implements AutoCloseable {
    private final Scanner scanner;

    public ScannerHelper() {
        this.scanner = new Scanner(System.in);
    }

    /**
     * Obtém a próxima entrada numérica inteira do usuário.
     * 
     * @return o número inteiro digitado pelo usuário
     */
    public int nextInt() {
        while (!scanner.hasNextInt()) {
            System.out.println("Por favor, insira um valor válido.");
            scanner.next();
        }

        int number = scanner.nextInt();
        scanner.nextLine();
        return number;
    }

    /**
     * Fecha o scanner quando o objeto é fechado (try-with-resources).
     */
    @Override
    public void close() {
        scanner.close();
    }
}