package services;

import entities.Question;
import entities.Quiz;
import entities.QuizResult;
import entities.QuizResultDetail;
import entities.Reponse;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.Color;
import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PdfExportService {

    private static final DateTimeFormatter FILE_DATE = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final DateTimeFormatter DISPLAY_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, new Color(15, 23, 42));
    private static final Font SECTION_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new Color(29, 78, 216));
    private static final Font LABEL_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, new Color(15, 23, 42));
    private static final Font BODY_FONT = FontFactory.getFont(FontFactory.HELVETICA, 10, new Color(30, 41, 59));
    private static final Font MUTED_FONT = FontFactory.getFont(FontFactory.HELVETICA, 9, new Color(100, 116, 139));
    private static final Font FOOTER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, new Color(100, 116, 139));

    private final QuestionCRUD questionCRUD = new QuestionCRUD();
    private final ReponseCRUD reponseCRUD = new ReponseCRUD();

    public File exportQuiz(Quiz quiz, boolean includeCorrections) throws SQLException, IOException, DocumentException {
        if (quiz == null) {
            throw new IllegalArgumentException("Aucun quiz selectionne.");
        }

        List<Question> questions = questionCRUD.afficherParQuiz(quiz.getId());
        File file = createExportFile("quiz_" + sanitize(quiz.getNomQuiz()));

        Document document = new Document(PageSize.A4, 36, 36, 48, 48);
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));
        writer.setPageEvent(new SkilloraFooter());
        document.open();

        addTitle(document, "Quiz : " + textOrFallback(quiz.getNomQuiz(), "Sans titre"));
        addMeta(document, "Niveau", quiz.getNiveau());
        addMeta(document, "Matiere", quiz.getMatiereValue());
        if (quiz.getDateAjout() != null) {
            addMeta(document, "Date d'ajout", DISPLAY_DATE.format(quiz.getDateAjout()));
        }
        if (quiz.getDescription() != null && !quiz.getDescription().isBlank()) {
            addSection(document, "Description");
            document.add(new Paragraph(quiz.getDescription(), BODY_FONT));
        }

        addSection(document, includeCorrections ? "Questions avec corrige" : "Questions");
        for (int i = 0; i < questions.size(); i++) {
            Question question = questions.get(i);
            addQuestionBlock(document, i + 1, question, includeCorrections);
        }

        if (questions.isEmpty()) {
            document.add(new Paragraph("Ce quiz ne contient pas encore de questions.", BODY_FONT));
        }

        document.close();
        return file;
    }

    public File exportResult(QuizResult result) throws IOException, DocumentException {
        if (result == null) {
            throw new IllegalArgumentException("Aucun resultat disponible.");
        }

        File file = createExportFile("resultats_" + sanitize(result.getQuizTitle()));
        Document document = new Document(PageSize.A4.rotate(), 30, 30, 42, 42);
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));
        writer.setPageEvent(new SkilloraFooter());
        document.open();

        addTitle(document, "Resultats du quiz");
        addMeta(document, "Utilisateur", textOrFallback(result.getUserName(), "Non disponible"));
        addMeta(document, "Quiz", result.getQuizTitle());
        addMeta(document, "Score total", result.getFinalPoints() + " / " + result.getTotalPoints() + " pts");
        addMeta(document, "Pourcentage", String.format("%.1f %%", result.getSuccessPercentage()));
        addMeta(document, "Bonnes reponses", String.valueOf(result.getCorrectAnswers()));
        addMeta(document, "Mauvaises reponses", String.valueOf(result.getIncorrectAnswers()));
        addMeta(document, "Bonus rapidite", "+" + result.getBonusPoints() + " pt(s)");
        addMeta(document, "Anti-triche", result.getAntiCheatSummary());
        addMeta(document, "Incidents critiques", String.valueOf(result.getAntiCheatCriticalCount()));
        if (result.isTerminatedByAntiCheat()) {
            addMeta(document, "Statut anti-triche", "Quiz termine automatiquement apres incidents repetes");
        }
        if (result.getAntiCheatReportPath() != null && !result.getAntiCheatReportPath().isBlank()) {
            addMeta(document, "Rapport anti-triche", result.getAntiCheatReportPath());
        }
        addMeta(document, "Date de realisation", result.getCompletedAt() == null
                ? "Non disponible"
                : DISPLAY_DATE.format(result.getCompletedAt()));

        addSection(document, "Detail question par question");
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{0.7f, 3.2f, 2.4f, 2.4f, 1.2f});
        addHeaderCell(table, "#");
        addHeaderCell(table, "Question");
        addHeaderCell(table, "Reponse donnee");
        addHeaderCell(table, "Bonne reponse");
        addHeaderCell(table, "Statut");

        List<QuizResultDetail> details = result.getDetails();
        for (int i = 0; i < details.size(); i++) {
            QuizResultDetail detail = details.get(i);
            addBodyCell(table, String.valueOf(i + 1));
            addBodyCell(table, textOrFallback(detail.getQuestionText(), "-"));
            addBodyCell(table, joinAnswers(detail.getSelectedAnswers()));
            addBodyCell(table, joinAnswers(detail.getCorrectAnswers()));
            addBodyCell(table, detail.isCorrect() ? "Correct" : "Incorrect");
        }

        if (details.isEmpty()) {
            addBodyCell(table, "-");
            addBodyCell(table, "Aucun detail disponible.");
            addBodyCell(table, "-");
            addBodyCell(table, "-");
            addBodyCell(table, "-");
        }

        document.add(table);
        document.close();
        return file;
    }

    public void openPdf(File file) throws IOException {
        ensureDesktopAction(Desktop.Action.OPEN);
        Desktop.getDesktop().open(file);
    }

    public void printPdf(File file) throws IOException {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.PRINT)) {
            Desktop.getDesktop().print(file);
            return;
        }
        openPdf(file);
    }

    public void openExportLocation(File file) throws IOException {
        if (file == null || file.getParentFile() == null) {
            return;
        }
        if (isWindows()) {
            String folderPath = file.getParentFile().getAbsolutePath();
            new ProcessBuilder("explorer.exe", folderPath).start();
            return;
        }
        ensureDesktopAction(Desktop.Action.OPEN);
        Desktop.getDesktop().open(file.getParentFile());
    }

    private boolean isWindows() {
        String os = System.getProperty("os.name", "");
        return os.toLowerCase().contains("win");
    }

    private void addQuestionBlock(Document document, int index, Question question, boolean includeCorrections)
            throws SQLException, DocumentException {
        Paragraph title = new Paragraph(index + ". " + textOrFallback(question.getEnonce(), "Question"), LABEL_FONT);
        title.setSpacingBefore(10);
        title.setSpacingAfter(4);
        document.add(title);
        addMeta(document, "Type", textOrFallback(question.getTypeQuestionValue(), "Non defini"));

        List<Reponse> reponses = reponseCRUD.afficherReponsesParQuestion(question.getId());
        if (reponses.isEmpty()) {
            document.add(new Paragraph("Aucune reponse disponible.", MUTED_FONT));
            return;
        }

        com.lowagie.text.List list = new com.lowagie.text.List(com.lowagie.text.List.UNORDERED);
        list.setIndentationLeft(18);
        for (Reponse reponse : reponses) {
            String line = textOrFallback(reponse.getTexte(), "-");
            if (includeCorrections && reponse.isCorrecte()) {
                line += "  [bonne reponse]";
            }
            list.add(new com.lowagie.text.ListItem(line, BODY_FONT));
        }
        document.add(list);
    }

    private void addTitle(Document document, String title) throws DocumentException {
        Paragraph paragraph = new Paragraph(title, TITLE_FONT);
        paragraph.setAlignment(Element.ALIGN_CENTER);
        paragraph.setSpacingAfter(12);
        document.add(paragraph);
    }

    private void addSection(Document document, String section) throws DocumentException {
        Paragraph paragraph = new Paragraph(section, SECTION_FONT);
        paragraph.setSpacingBefore(14);
        paragraph.setSpacingAfter(8);
        document.add(paragraph);
    }

    private void addMeta(Document document, String label, String value) throws DocumentException {
        Paragraph paragraph = new Paragraph();
        paragraph.add(new Chunk(label + " : ", LABEL_FONT));
        paragraph.add(new Chunk(textOrFallback(value, "Non disponible"), BODY_FONT));
        paragraph.setSpacingAfter(3);
        document.add(paragraph);
    }

    private void addHeaderCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, LABEL_FONT));
        cell.setBackgroundColor(new Color(219, 234, 254));
        cell.setBorderColor(new Color(191, 219, 254));
        cell.setPadding(7);
        table.addCell(cell);
    }

    private void addBodyCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(textOrFallback(text, "-"), BODY_FONT));
        cell.setBorderColor(new Color(226, 232, 240));
        cell.setPadding(7);
        table.addCell(cell);
    }

    private File createExportFile(String prefix) throws IOException {
        Path exportDir = resolveDownloadsDirectory();
        Files.createDirectories(exportDir);
        String fileName = prefix + "_" + LocalDateTime.now().format(FILE_DATE) + ".pdf";
        return exportDir.resolve(fileName).toFile();
    }

    private Path resolveDownloadsDirectory() {
        Path userHome = Path.of(System.getProperty("user.home", "."));
        Path downloads = userHome.resolve("Downloads");
        if (Files.exists(downloads)) {
            return downloads;
        }

        Path telechargements = userHome.resolve("Téléchargements");
        if (Files.exists(telechargements)) {
            return telechargements;
        }

        return downloads;
    }

    private String sanitize(String value) {
        String safe = value == null || value.isBlank() ? "document" : value.trim().toLowerCase();
        safe = safe.replaceAll("[^a-z0-9]+", "_");
        safe = safe.replaceAll("_+", "_");
        safe = safe.replaceAll("^_|_$", "");
        return safe.isBlank() ? "document" : safe;
    }

    private String textOrFallback(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String joinAnswers(List<String> answers) {
        if (answers == null || answers.isEmpty()) {
            return "Aucune reponse";
        }
        return String.join("\n", answers);
    }

    private void ensureDesktopAction(Desktop.Action action) throws IOException {
        if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(action)) {
            throw new IOException("Action systeme non supportee: " + action);
        }
    }

    private static class SkilloraFooter extends PdfPageEventHelper {
        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            Rectangle pageSize = document.getPageSize();
            ColumnText.showTextAligned(
                    writer.getDirectContent(),
                    Element.ALIGN_CENTER,
                    new Phrase("Skillora", FOOTER_FONT),
                    (pageSize.getLeft() + pageSize.getRight()) / 2,
                    pageSize.getBottom() + 20,
                    0
            );
        }
    }
}
