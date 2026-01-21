package com.inventario.util;

import com.inventario.dto.*;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class PdfGenerator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    /**
     * Genera ticket de venta en PDF
     */
    public byte[] generarTicketVenta(VentaDTO venta) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf, PageSize.A7);
        document.setMargins(10, 10, 10, 10);

        PdfFont fontBold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        PdfFont fontNormal = PdfFontFactory.createFont(StandardFonts.HELVETICA);

        // Encabezado
        document.add(new Paragraph("ALMACÉN DE REPUESTOS")
                .setFont(fontBold)
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER));

        document.add(new Paragraph("MOTOCICLETAS")
                .setFont(fontNormal)
                .setFontSize(8)
                .setTextAlignment(TextAlignment.CENTER));

        document.add(new Paragraph("------------------------")
                .setTextAlignment(TextAlignment.CENTER));

        // Información de venta
        document.add(new Paragraph("Venta #" + venta.getId())
                .setFont(fontBold)
                .setFontSize(8));

        document.add(new Paragraph("Fecha: " + venta.getFechaHora().format(DATETIME_FORMATTER))
                .setFont(fontNormal)
                .setFontSize(7));

        document.add(new Paragraph("------------------------")
                .setTextAlignment(TextAlignment.CENTER));

        // Detalles de productos
        for (DetalleVentaDTO detalle : venta.getDetalles()) {
            document.add(new Paragraph(detalle.getProductoNombre())
                    .setFont(fontNormal)
                    .setFontSize(7));
            document.add(new Paragraph(String.format("  %d x $%.2f = $%.2f",
                    detalle.getCantidad(),
                    detalle.getPrecioUnitario(),
                    detalle.getSubtotal()))
                    .setFont(fontNormal)
                    .setFontSize(7));
        }

        document.add(new Paragraph("------------------------")
                .setTextAlignment(TextAlignment.CENTER));

        // Total
        document.add(new Paragraph(String.format("TOTAL: $%.2f", venta.getTotal()))
                .setFont(fontBold)
                .setFontSize(10)
                .setTextAlignment(TextAlignment.RIGHT));

        document.add(new Paragraph("------------------------")
                .setTextAlignment(TextAlignment.CENTER));

        document.add(new Paragraph("¡Gracias por su compra!")
                .setFont(fontNormal)
                .setFontSize(7)
                .setTextAlignment(TextAlignment.CENTER));

        document.close();
        return baos.toByteArray();
    }

    /**
     * Genera reporte de inventario en PDF
     */
    public byte[] generarReporteInventario(List<ProductoDTO> productos, ReporteInventarioDTO resumen) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf, PageSize.A4);

        PdfFont fontBold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        PdfFont fontNormal = PdfFontFactory.createFont(StandardFonts.HELVETICA);

        // Título
        document.add(new Paragraph("REPORTE DE INVENTARIO")
                .setFont(fontBold)
                .setFontSize(18)
                .setTextAlignment(TextAlignment.CENTER));

        document.add(new Paragraph("Fecha: " + LocalDateTime.now().format(DATETIME_FORMATTER))
                .setFont(fontNormal)
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER));

        document.add(new Paragraph("\n"));

        // Resumen
        document.add(new Paragraph("RESUMEN")
                .setFont(fontBold)
                .setFontSize(14));

        Table resumenTable = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                .setWidth(UnitValue.createPercentValue(100));

        resumenTable.addCell(createCell("Total de Productos:", fontNormal));
        resumenTable.addCell(createCell(String.valueOf(resumen.getTotalProductos()), fontNormal));
        resumenTable.addCell(createCell("Productos con Stock:", fontNormal));
        resumenTable.addCell(createCell(String.valueOf(resumen.getProductosConStock()), fontNormal));
        resumenTable.addCell(createCell("Productos Agotados:", fontNormal));
        resumenTable.addCell(createCell(String.valueOf(resumen.getProductosAgotados()), fontNormal));
        resumenTable.addCell(createCell("Productos Stock Bajo:", fontNormal));
        resumenTable.addCell(createCell(String.valueOf(resumen.getProductosStockBajo()), fontNormal));
        resumenTable.addCell(createCell("Valor Total Inventario:", fontBold));
        resumenTable.addCell(createCell(String.format("$%.2f", resumen.getValorTotalInventario()), fontBold));

        document.add(resumenTable);
        document.add(new Paragraph("\n"));

        // Tabla de productos
        document.add(new Paragraph("DETALLE DE PRODUCTOS")
                .setFont(fontBold)
                .setFontSize(14));

        Table table = new Table(UnitValue.createPercentArray(new float[]{15, 30, 15, 10, 10, 20}))
                .setWidth(UnitValue.createPercentValue(100));

        // Encabezados
        table.addHeaderCell(createHeaderCell("Código", fontBold));
        table.addHeaderCell(createHeaderCell("Nombre", fontBold));
        table.addHeaderCell(createHeaderCell("Categoría", fontBold));
        table.addHeaderCell(createHeaderCell("Stock", fontBold));
        table.addHeaderCell(createHeaderCell("Precio", fontBold));
        table.addHeaderCell(createHeaderCell("Valor", fontBold));

        // Datos
        for (ProductoDTO producto : productos) {
            table.addCell(createCell(producto.getCodigoBarras(), fontNormal));
            table.addCell(createCell(producto.getNombre(), fontNormal));
            table.addCell(createCell(producto.getCategoriaNombre() != null ? producto.getCategoriaNombre() : "-", fontNormal));

            Cell stockCell = createCell(String.valueOf(producto.getStockActual()), fontNormal);
            if (Boolean.TRUE.equals(producto.getAgotado())) {
                stockCell.setBackgroundColor(ColorConstants.RED);
            } else if (Boolean.TRUE.equals(producto.getStockBajo())) {
                stockCell.setBackgroundColor(ColorConstants.YELLOW);
            }
            table.addCell(stockCell);

            table.addCell(createCell(String.format("$%.2f", producto.getPrecioVenta()), fontNormal));
            table.addCell(createCell(String.format("$%.2f",
                    producto.getPrecioVenta().multiply(java.math.BigDecimal.valueOf(producto.getStockActual()))), fontNormal));
        }

        document.add(table);

        document.close();
        return baos.toByteArray();
    }

    /**
     * Genera reporte de ventas en PDF
     */
    public byte[] generarReporteVentas(ReporteVentasDTO reporte) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf, PageSize.A4);

        PdfFont fontBold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        PdfFont fontNormal = PdfFontFactory.createFont(StandardFonts.HELVETICA);

        // Título
        document.add(new Paragraph("REPORTE DE VENTAS")
                .setFont(fontBold)
                .setFontSize(18)
                .setTextAlignment(TextAlignment.CENTER));

        document.add(new Paragraph(String.format("Período: %s - %s",
                reporte.getFechaInicio().format(DATE_FORMATTER),
                reporte.getFechaFin().format(DATE_FORMATTER)))
                .setFont(fontNormal)
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER));

        document.add(new Paragraph("\n"));

        // Resumen
        document.add(new Paragraph("RESUMEN")
                .setFont(fontBold)
                .setFontSize(14));

        Table resumenTable = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                .setWidth(UnitValue.createPercentValue(100));

        resumenTable.addCell(createCell("Total de Ventas:", fontNormal));
        resumenTable.addCell(createCell(String.format("$%.2f", reporte.getTotalVentas()), fontBold));
        resumenTable.addCell(createCell("Cantidad de Ventas:", fontNormal));
        resumenTable.addCell(createCell(String.valueOf(reporte.getCantidadVentas()), fontNormal));
        resumenTable.addCell(createCell("Productos Vendidos:", fontNormal));
        resumenTable.addCell(createCell(String.valueOf(reporte.getCantidadProductosVendidos()), fontNormal));
        resumenTable.addCell(createCell("Promedio por Venta:", fontNormal));
        resumenTable.addCell(createCell(String.format("$%.2f", reporte.getPromedioVenta()), fontNormal));

        document.add(resumenTable);
        document.add(new Paragraph("\n"));

        // Ventas diarias
        if (reporte.getVentasDiarias() != null && !reporte.getVentasDiarias().isEmpty()) {
            document.add(new Paragraph("VENTAS DIARIAS")
                    .setFont(fontBold)
                    .setFontSize(14));

            Table ventasTable = new Table(UnitValue.createPercentArray(new float[]{40, 30, 30}))
                    .setWidth(UnitValue.createPercentValue(100));

            ventasTable.addHeaderCell(createHeaderCell("Fecha", fontBold));
            ventasTable.addHeaderCell(createHeaderCell("Cantidad", fontBold));
            ventasTable.addHeaderCell(createHeaderCell("Total", fontBold));

            for (VentaDiariaDTO venta : reporte.getVentasDiarias()) {
                ventasTable.addCell(createCell(venta.getFecha().format(DATE_FORMATTER), fontNormal));
                ventasTable.addCell(createCell(String.valueOf(venta.getCantidadVentas()), fontNormal));
                ventasTable.addCell(createCell(String.format("$%.2f", venta.getTotal()), fontNormal));
            }

            document.add(ventasTable);
            document.add(new Paragraph("\n"));
        }

        // Productos más vendidos
        if (reporte.getProductosMasVendidos() != null && !reporte.getProductosMasVendidos().isEmpty()) {
            document.add(new Paragraph("PRODUCTOS MÁS VENDIDOS")
                    .setFont(fontBold)
                    .setFontSize(14));

            Table productosTable = new Table(UnitValue.createPercentArray(new float[]{20, 50, 30}))
                    .setWidth(UnitValue.createPercentValue(100));

            productosTable.addHeaderCell(createHeaderCell("Código", fontBold));
            productosTable.addHeaderCell(createHeaderCell("Producto", fontBold));
            productosTable.addHeaderCell(createHeaderCell("Cantidad Vendida", fontBold));

            for (ProductoMasVendidoDTO producto : reporte.getProductosMasVendidos()) {
                productosTable.addCell(createCell(producto.getCodigoBarras(), fontNormal));
                productosTable.addCell(createCell(producto.getNombre(), fontNormal));
                productosTable.addCell(createCell(String.valueOf(producto.getCantidadVendida()), fontNormal));
            }

            document.add(productosTable);
        }

        document.close();
        return baos.toByteArray();
    }

    private Cell createCell(String text, PdfFont font) {
        return new Cell().add(new Paragraph(text).setFont(font).setFontSize(9));
    }

    private Cell createHeaderCell(String text, PdfFont font) {
        return new Cell()
                .add(new Paragraph(text).setFont(font).setFontSize(9))
                .setBackgroundColor(ColorConstants.LIGHT_GRAY);
    }
}
package com.inventario.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Sistema de Inventario - Repuestos de Motos")
                        .version("1.0.0")
                        .description("API REST para la gestión de inventario de repuestos de motocicletas. " +
                                "Permite administrar productos, ventas, proveedores, categorías y reportes.")
                        .contact(new Contact()
                                .name("Soporte Técnico")
                                .email("soporte@inventariomotos.com"))
                        .license(new License()
                                .name("Uso Privado")
                                .url("#")));
    }
}

