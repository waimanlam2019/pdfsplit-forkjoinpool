package com.waimanlam;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;

import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class PdfSplitterApp extends Application {

    private final ForkJoinPool pool = new ForkJoinPool();

    private final DateTimeFormatter folderFormat = DateTimeFormatter.ofPattern("yyMMdd_HHmmss");
    private final LocalDateTime programStart = LocalDateTime.now();

    @Override
    public void start(Stage stage) {
        Label label = new Label("Select a PDF file to split:");
        Button selectBtn = new Button("Select PDF");

        selectBtn.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF files", "*.pdf"));
            File selectedFile = chooser.showOpenDialog(stage);

            if (selectedFile != null) {
                try {
                    splitPdf(selectedFile);
                    label.setText("Splitting completed!");
                } catch (IOException ex) {
                    label.setText("Error: " + ex.getMessage());
                }
            }
        });

        VBox root = new VBox(20, label, selectBtn);
        root.setAlignment(Pos.CENTER);

        // Set window size to 80% of screen
        double width = Screen.getPrimary().getBounds().getWidth() * 0.8;
        double height = Screen.getPrimary().getBounds().getHeight() * 0.8;
        Scene scene = new Scene(root, width, height);

        stage.setScene(scene);
        stage.setTitle("PDF Splitter");
        stage.show();
    }

    private void splitPdf(File pdfFile) throws IOException {
        PDDocument document = PDDocument.load(pdfFile);
        int totalPages = document.getNumberOfPages();
        document.close();

        // Output directory
        String timestamp = programStart.format(folderFormat);
        File outputDir = new File(pdfFile.getParentFile(), "output_" + timestamp);
        outputDir.mkdir();

        // Launch task using ForkJoinPool
        pool.invoke(new PdfSplitTask(pdfFile, outputDir, 0, totalPages));
    }

    // RecursiveAction for splitting PDF pages
    static class PdfSplitTask extends RecursiveAction {
        private final File sourcePdf;
        private final File outputDir;
        private final int startPage;
        private final int endPage;
        private static final int THRESHOLD = 5;

        PdfSplitTask(File sourcePdf, File outputDir, int startPage, int endPage) {
            this.sourcePdf = sourcePdf;
            this.outputDir = outputDir;
            this.startPage = startPage;
            this.endPage = endPage;
        }

        @Override
        protected void compute() {
            if ((endPage - startPage) <= THRESHOLD) {
                try (PDDocument document = PDDocument.load(sourcePdf)) {
                    for (int i = startPage; i < endPage; i++) {
                        PDDocument singlePage = new PDDocument();
                        singlePage.addPage(document.getPage(i));
                        File outFile = new File(outputDir, "page_" + (i + 1) + ".pdf");
                        singlePage.save(outFile);
                        singlePage.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                int mid = (startPage + endPage) / 2;
                invokeAll(
                        new PdfSplitTask(sourcePdf, outputDir, startPage, mid),
                        new PdfSplitTask(sourcePdf, outputDir, mid, endPage)
                );
            }
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
