package services;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import entities.Cours;
import entities.Chapitre;
import java.io.FileOutputStream;

import java.time.format.DateTimeFormatter;

public class CertificateService {

    public String generateCertificate(Cours cours, String studentName) throws Exception {
        String fileName = "Certificat_" + cours.getTitre().replaceAll("\\s+", "_") + "_" + System.currentTimeMillis() + ".pdf";
        Document document = new Document(PageSize.A4.rotate());
        
        PdfWriter.getInstance(document, new FileOutputStream(fileName));
        document.open();

        // Style
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 36, Font.BOLD);
        Font subTitleFont = FontFactory.getFont(FontFactory.HELVETICA, 18);
        Font nameFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 28, Font.ITALIC);
        Font footerFont = FontFactory.getFont(FontFactory.HELVETICA, 12, Font.NORMAL);

        // Content
        Paragraph p1 = new Paragraph("\n\n\nCERTIFICAT DE RÉUSSITE", titleFont);
        p1.setAlignment(Element.ALIGN_CENTER);
        document.add(p1);

        Paragraph p2 = new Paragraph("\nCe certificat est fièrement décerné à", subTitleFont);
        p2.setAlignment(Element.ALIGN_CENTER);
        document.add(p2);

        Paragraph p3 = new Paragraph("\n" + studentName.toUpperCase(), nameFont);
        p3.setAlignment(Element.ALIGN_CENTER);
        document.add(p3);

        Paragraph p4 = new Paragraph("\nPour avoir complété avec succès le cours :", subTitleFont);
        p4.setAlignment(Element.ALIGN_CENTER);
        document.add(p4);

        Paragraph p5 = new Paragraph("\n" + cours.getTitre(), titleFont);
        p5.setAlignment(Element.ALIGN_CENTER);
        document.add(p5);

        if (cours.getCategorie() != null && !cours.getCategorie().isEmpty()) {
            Paragraph pTags = new Paragraph("\nSpécialités : " + cours.getCategorie(), footerFont);
            pTags.setAlignment(Element.ALIGN_CENTER);
            document.add(pTags);
        }

        Paragraph p6 = new Paragraph("\n\nDate de délivrance : " + java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), footerFont);
        p6.setAlignment(Element.ALIGN_CENTER);
        document.add(p6);

        Paragraph p7 = new Paragraph("\n\n\n\nL'équipe Skillora", subTitleFont);
        p7.setAlignment(Element.ALIGN_RIGHT);
        document.add(p7);

        document.close();
        return fileName;
    }

    public String generateChapterPDF(Chapitre chapitre) throws Exception {
        String fileName = "Chapitre_" + chapitre.getTitre().replaceAll("\\s+", "_") + "_" + System.currentTimeMillis() + ".pdf";
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, new FileOutputStream(fileName));
        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24, Font.BOLD);
        Font contentFont = FontFactory.getFont(FontFactory.HELVETICA, 12);

        Paragraph pTitle = new Paragraph(chapitre.getTitre(), titleFont);
        pTitle.setAlignment(Element.ALIGN_CENTER);
        document.add(pTitle);

        document.add(new Paragraph("\n\n"));

        Paragraph pContent = new Paragraph(chapitre.getContenu(), contentFont);
        document.add(pContent);

        document.add(new Paragraph("\n\n--- Fin du chapitre ---", FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10)));

        document.close();
        return fileName;
    }
}
