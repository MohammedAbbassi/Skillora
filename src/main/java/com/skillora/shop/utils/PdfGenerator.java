package com.skillora.shop.utils;

import com.skillora.shop.entities.User;
import com.skillora.shop.entities.Coupon;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.skillora.shop.MoneyFormat;
import com.skillora.shop.model.OrderLine;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Utilitaire pour la génération de factures au format PDF.
 */
public class PdfGenerator {

    private static final String FOLDER = "factures";
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Génère une facture PDF pour une commande donnée.
     *
     * @param idCommande L'identifiant de la commande.
     * @param user L'utilisateur ayant passé la commande.
     * @param lines La liste des produits commandés.
     * @param coupon Le coupon appliqué (optionnel).
     * @return Le fichier généré.
     * @throws IOException Si une erreur survient lors de la création du fichier.
     */
    public File generateInvoice(long idCommande, User user, List<OrderLine> lines, Coupon coupon) throws IOException {
        // S'assurer que le dossier existe
        File dir = new File(FOLDER);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String fileName = String.format("%s/facture_%d.pdf", FOLDER, idCommande);
        File pdfFile = new File(fileName);

        try (PdfWriter writer = new PdfWriter(pdfFile);
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {

            // En-tête : Titre de la boutique
            document.add(new Paragraph("SKILLORA")
                    .setFontSize(24)
                    .setBold()
                    .setFontColor(new DeviceRgb(79, 70, 229)) // Couleur Indigo du thème
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph("Boutique de Produits Éducatifs")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setItalic()
                    .setMarginBottom(20));

            // Informations de la commande
            Table infoTable = new Table(UnitValue.createPercentArray(new float[]{50, 50})).useAllAvailableWidth();
            
            infoTable.addCell(new Cell().add(new Paragraph("Client :")
                    .setBold())
                    .setBorder(null));
            infoTable.addCell(new Cell().add(new Paragraph("Facture N° : " + idCommande)
                    .setBold())
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setBorder(null));

            infoTable.addCell(new Cell().add(new Paragraph(user.getPrenom() + " " + user.getNom() + "\n" + user.getEmail()))
                    .setBorder(null));
            infoTable.addCell(new Cell().add(new Paragraph("Date : " + LocalDateTime.now().format(FMT)))
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setBorder(null));

            document.add(infoTable.setMarginBottom(20));

            // Tableau des produits
            Table table = new Table(UnitValue.createPercentArray(new float[]{40, 20, 20, 20})).useAllAvailableWidth();
            
            // En-têtes du tableau
            table.addHeaderCell(new Cell().add(new Paragraph("Produit").setBold()).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            table.addHeaderCell(new Cell().add(new Paragraph("Qté").setBold()).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            table.addHeaderCell(new Cell().add(new Paragraph("Prix Unit.").setBold()).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            table.addHeaderCell(new Cell().add(new Paragraph("Total").setBold()).setBackgroundColor(ColorConstants.LIGHT_GRAY));

            double totalProduitsHT = 0;
            for (OrderLine line : lines) {
                table.addCell(new Cell().add(new Paragraph(line.getNomProduit())));
                table.addCell(new Cell().add(new Paragraph(String.valueOf(line.getQuantite()))));
                table.addCell(new Cell().add(new Paragraph(MoneyFormat.amount(line.getPrixUnitaire()))));
                table.addCell(new Cell().add(new Paragraph(MoneyFormat.amount(line.getSousTotal()))));
                totalProduitsHT += line.getSousTotal();
            }

            document.add(table);

            // Totaux
            Paragraph totals = new Paragraph()
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setMarginTop(20);
            
            totals.add("Total produits : " + MoneyFormat.amount(totalProduitsHT) + "\n");

            double discount = 0;
            if (coupon != null) {
                if (coupon.getTypeReduction() == Coupon.TypeReduction.POURCENTAGE) {
                    discount = totalProduitsHT * (coupon.getValeurReduction() / 100.0);
                    totals.add(String.format("Coupon '%s' (-%.0f%%) : -%s\n", 
                            coupon.getCode(), coupon.getValeurReduction(), MoneyFormat.amount(discount)));
                } else {
                    discount = Math.min(coupon.getValeurReduction(), totalProduitsHT);
                    totals.add(String.format("Coupon '%s' : -%s\n", 
                            coupon.getCode(), MoneyFormat.amount(discount)));
                }
            }

            double finalTotal = totalProduitsHT - discount;

            totals.add(new Paragraph("TOTAL À PAYER : " + MoneyFormat.amount(finalTotal))
                    .setBold()
                    .setFontSize(14)
                    .setFontColor(new DeviceRgb(34, 197, 94))); // Couleur verte

            document.add(totals);

            // Pied de page
            document.add(new Paragraph("\n\nMerci de votre confiance !")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(ColorConstants.GRAY)
                    .setItalic());
        }

        return pdfFile;
    }

    /**
     * Ouvre automatiquement le fichier PDF.
     *
     * @param file Le fichier à ouvrir.
     */
    public void openPdf(File file) {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().open(file);
            } catch (IOException e) {
                System.err.println("Impossible d'ouvrir le fichier : " + e.getMessage());
            }
        }
    }
}
