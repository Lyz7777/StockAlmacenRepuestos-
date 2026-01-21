package com.inventario.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;
import com.google.zxing.oned.EAN13Writer;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Component
public class BarcodeGenerator {

    private static final SecureRandom random = new SecureRandom();

    /**
     * Genera un código de barras único de 13 dígitos (formato EAN-13)
     */
    public String generarCodigoBarras() {
        StringBuilder codigo = new StringBuilder();

        // Prefijo interno (799 para uso interno)
        codigo.append("799");

        // 9 dígitos aleatorios
        for (int i = 0; i < 9; i++) {
            codigo.append(random.nextInt(10));
        }

        // Calcular dígito de control EAN-13
        String codigoSinDigitoControl = codigo.toString();
        int digitoControl = calcularDigitoControlEAN13(codigoSinDigitoControl);
        codigo.append(digitoControl);

        return codigo.toString();
    }

    /**
     * Genera un código interno único
     */
    public String generarCodigoInterno(String prefijo) {
        long timestamp = System.currentTimeMillis() % 100000;
        int aleatorio = random.nextInt(1000);
        return String.format("%s-%05d-%03d", prefijo != null ? prefijo : "PRD", timestamp, aleatorio);
    }

    /**
     * Calcula el dígito de control para EAN-13
     */
    private int calcularDigitoControlEAN13(String codigo) {
        int suma = 0;
        for (int i = 0; i < 12; i++) {
            int digito = Character.getNumericValue(codigo.charAt(i));
            suma += (i % 2 == 0) ? digito : digito * 3;
        }
        int resto = suma % 10;
        return (resto == 0) ? 0 : 10 - resto;
    }

    /**
     * Genera imagen del código de barras en formato PNG como Base64
     */
    public String generarImagenCodigoBarras(String codigo, int ancho, int alto) {
        try {
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.MARGIN, 1);

            BitMatrix bitMatrix;

            // Usar EAN-13 si el código tiene 13 dígitos, sino usar Code128
            if (codigo.length() == 13 && codigo.matches("\\d+")) {
                EAN13Writer writer = new EAN13Writer();
                bitMatrix = writer.encode(codigo, BarcodeFormat.EAN_13, ancho, alto, hints);
            } else {
                Code128Writer writer = new Code128Writer();
                bitMatrix = writer.encode(codigo, BarcodeFormat.CODE_128, ancho, alto, hints);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);

            byte[] imageBytes = outputStream.toByteArray();
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes);

        } catch (WriterException | IOException e) {
            throw new RuntimeException("Error al generar imagen del código de barras: " + e.getMessage(), e);
        }
    }

    /**
     * Genera imagen del código de barras con tamaño por defecto (300x100)
     */
    public String generarImagenCodigoBarras(String codigo) {
        return generarImagenCodigoBarras(codigo, 300, 100);
    }

    /**
     * Valida si un código de barras EAN-13 es válido
     */
    public boolean validarCodigoEAN13(String codigo) {
        if (codigo == null || codigo.length() != 13 || !codigo.matches("\\d+")) {
            return false;
        }

        String codigoSinDigitoControl = codigo.substring(0, 12);
        int digitoControlCalculado = calcularDigitoControlEAN13(codigoSinDigitoControl);
        int digitoControlActual = Character.getNumericValue(codigo.charAt(12));

        return digitoControlCalculado == digitoControlActual;
    }
}

