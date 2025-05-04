package com.ibra.advancedtextprocessor;

import com.ibra.advancedtextprocessor.backend.*;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class AdvancedTextProcessor extends Application {

    // Backend components
    private final TextProcessor textProcessor = new TextProcessor();
    private final FileProcessor fileProcessor = new FileProcessor();
    private final DataAnalyzer dataAnalyzer = new DataAnalyzer();
    private final PatternManager patternManager = PatternManager.createCommonPatternCollection();

    // UI Components - Input Section
    private TitledPane inputPane;
    private TextArea mainInputTextArea;
    private RadioButton textInputRadio;
    private RadioButton fileInputRadio;
    private RadioButton batchInputRadio;
    private TextField filePathField;
    private ListView<File> batchFilesListView;
    private ProgressBar progressBar;

    // UI Components - Regex Processing Section
    private TitledPane regexPane;
    private TextField regexPatternField;
    private CheckBox multilineCheckBox;
    private CheckBox caseInsensitiveCheckBox;
    private ComboBox<String> savedPatternsComboBox;
    private ObservableList<String> savedPatterns = FXCollections.observableArrayList();
    private TextField replacementTextField;
    private CheckBox highlightMatchesCheckBox;
    private CheckBox findMatchesCheckBox;
    private CheckBox replaceMatchesCheckBox;
    private TextField highlightPrefixField;
    private TextField highlightSuffixField;
    private ListView<String> matchesListView;

    // UI Components - Analysis Section
    private TitledPane analysisPane;
    private CheckBox wordFrequencyCheckBox;
    private CheckBox summarizationCheckBox;
    private TextField minWordLengthField;
    private TextField maxSentencesField;

    // UI Components - Output Section
    private TitledPane outputPane;
    private TextArea mainOutputTextArea;
    private Label statusLabel;
    private TextArea fileContentPreviewArea; // Added to store the preview area
    private TextField outputDirField;

    // Data storage
    private Map<String, Long> wordFrequencyMap = new HashMap<>();
    private List<String> searchHistory = new ArrayList<>();

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("DataFlow Text Processor");

        // Create the main layout
        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(10));

        // Create accordion for collapsible sections
        Accordion accordion = new Accordion();

        // Create sections
        inputPane = createInputSection();
        regexPane = createRegexProcessingSection();
        analysisPane = createAnalysisSection();
        outputPane = createOutputSection();

        // Add sections to accordion
        accordion.getPanes().addAll(inputPane, regexPane, analysisPane, outputPane);

        // Expand the input pane by default
        accordion.setExpandedPane(inputPane);

        // Add accordion to the main layout
        mainLayout.setCenter(accordion);

        // Create status bar
        statusLabel = new Label("Ready");
        HBox statusBar = new HBox(statusLabel);
        statusBar.setPadding(new Insets(5));
        statusBar.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc; -fx-border-width: 1 0 0 0;");
        mainLayout.setBottom(statusBar);

        // Create scene and show
        Scene scene = new Scene(mainLayout, 900, 700);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private TitledPane createInputSection() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        // Input source selection
        ToggleGroup inputSourceGroup = new ToggleGroup();
        textInputRadio = new RadioButton("Direct Text Input");
        fileInputRadio = new RadioButton("File Input");
        batchInputRadio = new RadioButton("Batch File Processing");

        textInputRadio.setToggleGroup(inputSourceGroup);
        fileInputRadio.setToggleGroup(inputSourceGroup);
        batchInputRadio.setToggleGroup(inputSourceGroup);
        textInputRadio.setSelected(true);

        HBox radioButtonsBox = new HBox(20);
        radioButtonsBox.getChildren().addAll(textInputRadio, fileInputRadio, batchInputRadio);

        // Text input area
        mainInputTextArea = new TextArea();
        mainInputTextArea.setPrefRowCount(10);
        mainInputTextArea.setPromptText("Enter text to process here...");
        mainInputTextArea.setWrapText(true);


        // File selection components
        HBox fileSelectionBox = new HBox(10);
        filePathField = new TextField();
        filePathField.setPromptText("File path");
        filePathField.setPrefWidth(400);
        Button browseButton = new Button("Browse");
        fileSelectionBox.getChildren().addAll(filePathField, browseButton);
        fileSelectionBox.setAlignment(Pos.CENTER_LEFT);

        // Batch files list
        VBox batchFilesBox = new VBox(10);
        Label batchFilesLabel = new Label("Files for batch processing:");
        batchFilesListView = new ListView<>();
        batchFilesListView.setPrefHeight(150);
        batchFilesListView.isEditable();

        HBox batchButtonsBox = new HBox(10);
        Button addFilesButton = new Button("Add Files");
        Button clearFilesButton = new Button("Clear All");
        batchButtonsBox.getChildren().addAll(addFilesButton, clearFilesButton);

        // In the createInputSection() method, add a "Process Batch" button
// Near the batchButtonsBox area where you have the addFilesButton and clearFilesButton

// Add after this line: batchButtonsBox.getChildren().addAll(addFilesButton, clearFilesButton);
        Button processBatchButton = new Button("Process Batch Files");
        processBatchButton.setOnAction(e -> executeBatchProcessing());
        batchButtonsBox.getChildren().add(processBatchButton);

// Add a preview area for batch results
        VBox previewBox = new VBox(10);
        Label previewLabel = new Label("Batch Processing Preview:");
        fileContentPreviewArea = new TextArea();
        fileContentPreviewArea.setPrefHeight(200);
        fileContentPreviewArea.setEditable(false);
        fileContentPreviewArea.setWrapText(true);
        previewBox.getChildren().addAll(previewLabel, fileContentPreviewArea);
        batchFilesBox.getChildren().add(previewBox);

        // Batch output directory
        HBox batchOutputBox = new HBox(10);
        Label outputDirLabel = new Label("Output Directory:");
        outputDirField = new TextField();
        outputDirField.setPrefWidth(300);
        Button browseOutputDirButton = new Button("Browse");
        batchOutputBox.getChildren().addAll(outputDirLabel, outputDirField, browseOutputDirButton);

        batchFilesBox.getChildren().addAll(batchFilesLabel, batchFilesListView, batchButtonsBox, batchOutputBox);

        // Progress bar for batch operations
        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(Double.MAX_VALUE);
        progressBar.setVisible(false);

        // Add all components to the content pane
        content.getChildren().addAll(
                new Label("Input Source:"),
                radioButtonsBox,
                mainInputTextArea,
                fileSelectionBox,
                batchFilesBox,
                progressBar
        );

        // Set initial visibility
        fileSelectionBox.setVisible(false);
        batchFilesBox.setVisible(false);

        // Add listeners for radio buttons
        textInputRadio.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                mainInputTextArea.setVisible(true);
                fileSelectionBox.setVisible(false);
                batchFilesBox.setVisible(false);
                progressBar.setVisible(false);
            }
        });

        fileInputRadio.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                mainInputTextArea.setVisible(true);
                fileSelectionBox.setVisible(true);
                batchFilesBox.setVisible(false);
                progressBar.setVisible(false);
            }
        });

        batchInputRadio.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                mainInputTextArea.setVisible(false);
                fileSelectionBox.setVisible(false);
                batchFilesBox.setVisible(true);
                progressBar.setVisible(true);
            }
        });

        // File browser action
        browseButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Text File");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Text Files", "*.txt"),
                    new FileChooser.ExtensionFilter("All Files", "*.*")
            );
            File selectedFile = fileChooser.showOpenDialog(null);
            if (selectedFile != null) {
                filePathField.setText(selectedFile.getAbsolutePath());
                loadFile(selectedFile, mainInputTextArea);
            }
        });

        // Add files to batch processing
        addFilesButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Files for Batch Processing");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Text Files", "*.txt")
            );
            List<File> files = fileChooser.showOpenMultipleDialog(null);
            if (files != null) {
                batchFilesListView.getItems().addAll(files);
            }
        });

        // Clear batch files
        clearFilesButton.setOnAction(e -> batchFilesListView.getItems().clear());

        // Browse for output directory
        browseOutputDirButton.setOnAction(e -> {
            DirectoryChooser dirChooser = new DirectoryChooser();
            File selectedDir = dirChooser.showDialog(null);
            if (selectedDir != null) {
                outputDirField.setText(selectedDir.getAbsolutePath());
                // Auto-populate files list
                populateBatchFilesList(selectedDir);
            }
        });

        // Create the titled pane
        TitledPane pane = new TitledPane("1. Input Source", content);
        pane.setCollapsible(true);
        return pane;
    }

    private TitledPane createRegexProcessingSection() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        // Pattern input section
        Label patternLabel = new Label("Regex Pattern:");
        regexPatternField = new TextField();
        regexPatternField.setPromptText("Enter regex pattern");

        // Regex options
        HBox optionsBox = new HBox(10);
        multilineCheckBox = new CheckBox("Multiline");
        caseInsensitiveCheckBox = new CheckBox("Case Insensitive");
        optionsBox.getChildren().addAll(multilineCheckBox, caseInsensitiveCheckBox);

        // Saved patterns
        HBox savedPatternsBox = new HBox(10);
        savedPatternsComboBox = new ComboBox<>(savedPatterns);
        savedPatternsComboBox.setPromptText("Saved Patterns");
        Button savePatternButton = new Button("Save Pattern");
        savePatternButton.setOnAction(e -> saveCurrentPattern());
        Button loadPatternButton = new Button("Load");
        loadPatternButton.setOnAction(e -> loadSelectedPattern());
        savedPatternsBox.getChildren().addAll(savedPatternsComboBox, loadPatternButton, savePatternButton);

        // Operation selection
        VBox operationsBox = new VBox(10);
        Label operationsLabel = new Label("Select Operations:");

        // Find matches operation
        HBox findMatchesBox = new HBox(10);
        findMatchesCheckBox = new CheckBox("Find Matches");
        Button executeMatchesButton = new Button("Execute");
        executeMatchesButton.setOnAction(e -> findMatches());
        findMatchesBox.getChildren().addAll(findMatchesCheckBox, executeMatchesButton);

        // Highlight matches operation
        HBox highlightBox = new HBox(10);
        highlightMatchesCheckBox = new CheckBox("Highlight Matches");
        highlightPrefixField = new TextField("**");
        highlightSuffixField = new TextField("**");
        highlightPrefixField.setPrefWidth(60);
        highlightSuffixField.setPrefWidth(60);
        highlightBox.getChildren().addAll(
                highlightMatchesCheckBox,
                new Label("Prefix:"),
                highlightPrefixField,
                new Label("Suffix:"),
                highlightSuffixField
        );

        // Replace operation
        HBox replaceBox = new HBox(10);
        replaceMatchesCheckBox = new CheckBox("Replace Matches");
        replacementTextField = new TextField();
        replacementTextField.setPromptText("Replacement text");
        replacementTextField.setPrefWidth(300);
        replaceBox.getChildren().addAll(replaceMatchesCheckBox, new Label("Replace with:"), replacementTextField);

        operationsBox.getChildren().addAll(
                operationsLabel,
                findMatchesBox,
                highlightBox,
                replaceBox
        );

        // Execute button
        Button executeButton = new Button("Process Text with Selected Operations");
        executeButton.setOnAction(e -> processText());
        executeButton.setPrefWidth(250);

        HBox executeBox = new HBox(executeButton);
        executeBox.setAlignment(Pos.CENTER);
        executeBox.setPadding(new Insets(10, 0, 0, 0));

        // Matches list
        Label matchesLabel = new Label("Matches:");
        matchesListView = new ListView<>();
        matchesListView.setPrefHeight(120);

        // Add all components to the content pane
        content.getChildren().addAll(
                patternLabel,
                regexPatternField,
                optionsBox,
                savedPatternsBox,
                operationsBox,
                executeBox,
                matchesLabel,
                matchesListView
        );

        // Create the titled pane
        TitledPane pane = new TitledPane("2. Regex Processing", content);
        pane.setCollapsible(true);
        return pane;
    }

    private TitledPane createAnalysisSection() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        // Analysis options
        Label analysisLabel = new Label("Text Analysis Options:");

        // Word frequency analysis
        HBox wordFrequencyBox = new HBox(10);
        wordFrequencyCheckBox = new CheckBox("Word Frequency Analysis");
        minWordLengthField = new TextField("3");
        minWordLengthField.setPromptText("Min. word length");
        minWordLengthField.setPrefWidth(60);
        wordFrequencyBox.getChildren().addAll(
                wordFrequencyCheckBox,
                new Label("Min. word length:"),
                minWordLengthField
        );

        // Text summarization
        HBox summarizationBox = new HBox(10);
        summarizationCheckBox = new CheckBox("Text Summarization");
        maxSentencesField = new TextField("3");
        maxSentencesField.setPromptText("Max sentences");
        maxSentencesField.setPrefWidth(60);
        summarizationBox.getChildren().addAll(
                summarizationCheckBox,
                new Label("Max sentences:"),
                maxSentencesField
        );

        // Execute analysis button
        Button analyzeButton = new Button("Analyze Text");
        analyzeButton.setOnAction(e -> analyzeText());

        HBox analyzeButtonBox = new HBox(analyzeButton);
        analyzeButtonBox.setAlignment(Pos.CENTER);
        analyzeButtonBox.setPadding(new Insets(10, 0, 0, 0));

        // Add all components to the content pane
        content.getChildren().addAll(
                analysisLabel,
                wordFrequencyBox,
                summarizationBox,
                analyzeButtonBox
        );

        // Create the titled pane
        TitledPane pane = new TitledPane("3. Text Analysis", content);
        pane.setCollapsible(true);
        return pane;
    }

    private TitledPane createOutputSection() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        // Output text area
        Label outputLabel = new Label("Processing Results:");
        mainOutputTextArea = new TextArea();
        mainOutputTextArea.setPrefRowCount(15);
        mainOutputTextArea.setWrapText(true);
        mainOutputTextArea.setEditable(false);

        // Output actions
        HBox actionsBox = new HBox(10);
        Button saveOutputButton = new Button("Save to File");
        Button copyToInputButton = new Button("Copy to Input");
        Button clearOutputButton = new Button("Clear");

        actionsBox.getChildren().addAll(saveOutputButton, copyToInputButton, clearOutputButton);
        actionsBox.setAlignment(Pos.CENTER_RIGHT);

        // Add all components to the content pane
        content.getChildren().addAll(
                outputLabel,
                mainOutputTextArea,
                actionsBox
        );

        // Set actions
        saveOutputButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Output");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Text Files", "*.txt")
            );
            File outputFile = fileChooser.showSaveDialog(null);
            if (outputFile != null) {
                saveToFile(outputFile, mainOutputTextArea.getText());
            }
        });

        copyToInputButton.setOnAction(e -> {
            mainInputTextArea.setText(mainOutputTextArea.getText());
            statusLabel.setText("Output copied to input");
        });

        clearOutputButton.setOnAction(e -> {
            mainOutputTextArea.clear();
            statusLabel.setText("Output cleared");
        });

        // Create the titled pane
        TitledPane pane = new TitledPane("4. Output", content);
        pane.setCollapsible(true);
        return pane;
    }

    // Main processing methods

    private void processText() {
        try {
            String inputText = getCurrentInputText();
            if (inputText.isEmpty()) {
                showError("Error", "No input text to process");
                return;
            }

            String patternStr = regexPatternField.getText();
            if (patternStr.isEmpty() && (findMatchesCheckBox.isSelected() ||
                    highlightMatchesCheckBox.isSelected() ||
                    replaceMatchesCheckBox.isSelected())) {
                showError("Error", "Please enter a regex pattern");
                return;
            }

            int flags = getCurrentFlags();
            String result = inputText;

            // Process operations in sequence: find -> highlight -> replace
            if (findMatchesCheckBox.isSelected()) {
                findMatches();
            }

            if (highlightMatchesCheckBox.isSelected()) {
                result = textProcessor.highlightMatches(
                        result,
                        patternStr,
                        highlightPrefixField.getText(),
                        highlightSuffixField.getText(),
                        flags
                );
            }

            if (replaceMatchesCheckBox.isSelected()) {
                result = textProcessor.replaceAll(
                        result,
                        patternStr,
                        replacementTextField.getText(),
                        flags
                );
            }

            // Update output if any operation was performed
            if (highlightMatchesCheckBox.isSelected() || replaceMatchesCheckBox.isSelected()) {
                mainOutputTextArea.setText(result);
                statusLabel.setText("Text processed successfully");
            }

        } catch (PatternSyntaxException e) {
            showError("Invalid Regex Pattern", "The regex pattern is invalid: " + e.getMessage());
            statusLabel.setText("Error: Invalid regex pattern");
        } catch (Exception e) {
            showError("Error", e.getMessage());
            statusLabel.setText("Error: " + e.getMessage());
        }
    }

    private void findMatches() {
        try {
            String inputText = getCurrentInputText();
            String patternStr = regexPatternField.getText();

            if (patternStr.isEmpty()) {
                throw new IllegalArgumentException("Regex pattern cannot be empty");
            }

            List<String> matches = textProcessor.findMatches(
                    inputText,
                    patternStr,
                    getCurrentFlags()
            );

            matchesListView.getItems().clear();

            if (matches.isEmpty()) {
                statusLabel.setText("No matches found");
            } else {
                matchesListView.getItems().addAll(matches);
                statusLabel.setText("Found " + matches.size() + " matches");
            }

            // Add to search history
            if (!searchHistory.contains(patternStr)) {
                searchHistory.add(patternStr);
            }

        } catch (PatternSyntaxException e) {
            showError("Invalid Regex Pattern", "The regex pattern is invalid: " + e.getMessage());
            statusLabel.setText("Error: Invalid regex pattern");
        } catch (Exception e) {
            showError("Error", e.getMessage());
            statusLabel.setText("Error: " + e.getMessage());
        }
    }

    private void analyzeText() {
        try {
            String text = getCurrentInputText();
            if (text.isEmpty()) {
                throw new IllegalArgumentException("No text to analyze");
            }

            // Clear previous results
            mainOutputTextArea.clear();
            wordFrequencyMap.clear();

            StringBuilder resultBuilder = new StringBuilder();

            // Perform word frequency analysis if selected
            if (wordFrequencyCheckBox.isSelected()) {
                int minLength = parseField(minWordLengthField.getText(), 3, "Invalid minimum word length");

                // Use DataAnalyzer to perform the analysis
                wordFrequencyMap = dataAnalyzer.wordFrequencyAnalysis(text, minLength);

                // Format the results
                resultBuilder.append("Word Frequency Analysis:\n");
                resultBuilder.append(String.format("%-20s %s\n", "Word", "Frequency"));
                resultBuilder.append("----------------------------------------\n");

                // Display the word frequency results
                for (Map.Entry<String, Long> entry : wordFrequencyMap.entrySet()) {
                    resultBuilder.append(String.format("%-20s %d\n", entry.getKey(), entry.getValue()));
                }

                resultBuilder.append("\n\n");
            }

            // Perform text summarization if selected
            if (summarizationCheckBox.isSelected()) {
                int maxSentences = parseField(maxSentencesField.getText(), 3, "Invalid maximum sentences count");

                // Use DataAnalyzer to summarize text
                String summary = DataAnalyzer.summarizeText(text, maxSentences);

                resultBuilder.append("Text Summary:\n\n");
                resultBuilder.append(summary);
            }

            // Set the analysis results to the output area
            mainOutputTextArea.setText(resultBuilder.toString());
            statusLabel.setText("Analysis completed");

        } catch (Exception e) {
            showError("Analysis Error", e.getMessage());
            statusLabel.setText("Error: " + e.getMessage());
        }
    }

    // Helper methods

    private String getCurrentInputText() {
        if (textInputRadio.isSelected() || fileInputRadio.isSelected()) {
            return mainInputTextArea.getText();
        } else if (batchInputRadio.isSelected()) {
            // For batch mode, we don't have a single text to process
            // This should be handled separately in batch processing logic
            throw new IllegalStateException("Please select individual text or file input for this operation");
        }
        return "";
    }

    private int getCurrentFlags() {
        int flags = 0;
        if (multilineCheckBox.isSelected()) flags |= Pattern.MULTILINE;
        if (caseInsensitiveCheckBox.isSelected()) flags |= Pattern.CASE_INSENSITIVE;
        return flags;
    }

    private void saveCurrentPattern() {
        if (regexPatternField.getText().isEmpty()) {
            showError("Save Pattern", "Pattern cannot be empty");
            return;
        }

        PatternEntry entry = new PatternEntry(
                "Pattern " + (savedPatterns.size() + 1) + ": " + regexPatternField.getText(),
                regexPatternField.getText(),
                multilineCheckBox.isSelected(),
                caseInsensitiveCheckBox.isSelected()
        );

        if (patternManager.addPattern(entry)) {
            savedPatterns.add(entry.getName());
            statusLabel.setText("Pattern saved");
        }
    }

    private void loadSelectedPattern() {
        String selected = savedPatternsComboBox.getValue();
        PatternEntry entry = patternManager.getPattern(selected);
        if (entry != null) {
            regexPatternField.setText(entry.getPattern());
            multilineCheckBox.setSelected(entry.isMultiline());
            caseInsensitiveCheckBox.setSelected(entry.isCaseInsensitive());
            statusLabel.setText("Pattern loaded");
        }
    }

    private void loadFile(File file, TextArea targetTextArea) {
        try {
            String content = fileProcessor.readFile(file);
            targetTextArea.setText(content);
            statusLabel.setText("File loaded: " + file.getName());
        } catch (IOException e) {
            showError("File Loading Error", "Could not load file: " + e.getMessage());
            statusLabel.setText("Error loading file: " + e.getMessage());
        }
    }

    private void saveToFile(File file, String content) {
        try {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(content);
            }
            statusLabel.setText("File saved: " + file.getName());
        } catch (IOException e) {
            showError("File Saving Error", "Could not save file: " + e.getMessage());
            statusLabel.setText("Error saving file: " + e.getMessage());
        }
    }

    private void populateBatchFilesList(File directory) {
        if (directory == null || !directory.isDirectory()) {
            return;
        }

        // Clear current list
        batchFilesListView.getItems().clear();

        // Add all text files from the directory
        File[] files = directory.listFiles(file ->
                file.isFile() && file.getName().toLowerCase().endsWith(".txt"));

        if (files != null && files.length > 0) {
            batchFilesListView.getItems().addAll(files);
        }
    }

    private int parseField(String value, int defaultValue, String errorMessage) {
        if (value == null || value.trim().isEmpty()) return defaultValue;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    // Define the executeBatchProcessing method to call the batch processing methods
    private void executeBatchProcessing() {
        if (batchFilesListView.getItems().isEmpty()) {
            showError("Batch Processing Error", "No files selected for batch processing");
            return;
        }

        // Ensure the output directory is specified
        String outputDirPath = outputDirField.getText();
        if (outputDirPath == null || outputDirPath.trim().isEmpty()) {
            showError("Batch Processing Error", "Please select an output directory");
            return;
        }

        List<File> files = new ArrayList<>(batchFilesListView.getItems());
        File outputDir = new File(outputDirPath);

        // Validate output directory exists or can be created
        if (!outputDir.exists()) {
            boolean created = outputDir.mkdirs();
            if (!created) {
                showError("Batch Processing Error", "Could not create output directory: " + outputDirPath);
                return;
            }
        } else if (!outputDir.isDirectory()) {
            showError("Batch Processing Error", "The specified output path is not a directory");
            return;
        }

        // Check which operations need to be performed
        boolean performRegexOps = !regexPatternField.getText().isEmpty() &&
                (findMatchesCheckBox.isSelected() ||
                        highlightMatchesCheckBox.isSelected() ||
                        replaceMatchesCheckBox.isSelected());

        boolean performAnalysis = wordFrequencyCheckBox.isSelected() ||
                summarizationCheckBox.isSelected();

        if (!performRegexOps && !performAnalysis) {
            showError("Batch Processing Error", "Please select at least one operation to perform");
            return;
        }

        // Create a background task for batch processing
        new Thread(() -> {
            processBatchFilesInBackground(files, outputDir, performRegexOps, performAnalysis);
        }).start();
    }

    // Fix the processBatchFiles method - keep just one version and modify it
    private void processBatchFilesInBackground(List<File> files, File outputDir,
                                               boolean performRegexOps, boolean performAnalysis) {
        int totalFiles = files.size();
        int processedFiles = 0;

        // Collect all processing parameters
        String regexPattern = regexPatternField.getText();
        int flags = getCurrentFlags();
        boolean findMatches = findMatchesCheckBox.isSelected();
        boolean highlightMatches = highlightMatchesCheckBox.isSelected();
        boolean replaceMatches = replaceMatchesCheckBox.isSelected();
        String highlightPrefix = highlightPrefixField.getText();
        String highlightSuffix = highlightSuffixField.getText();
        String replacement = replacementTextField.getText();

        int minWordLength = parseField(minWordLengthField.getText(), 3, "Invalid minimum word length");
        int maxSentences = parseField(maxSentencesField.getText(), 3, "Invalid maximum sentences count");

        boolean wordFrequency = wordFrequencyCheckBox.isSelected();
        boolean summarization = summarizationCheckBox.isSelected();

        // Update UI
        Platform.runLater(() -> {
            statusLabel.setText("Batch processing started: 0/" + totalFiles + " files");
            progressBar.setProgress(0);
            progressBar.setVisible(true);
            mainOutputTextArea.clear();
        });

        // Results storage for analysis
        StringBuilder analysisResults = new StringBuilder();

        for (File file : files) {
            try {
                final File currentFile = file;

                // Read the file content
                String content = fileProcessor.readFile(file);
                String outputContent = content;
                final String fileName = file.getName();

                // Apply regex operations if needed
                if (performRegexOps) {
                    try {
                        // Apply operations in the same order as in processText()
                        if (findMatches) {
                            // For findMatches, we only collect the matches but don't modify the content
                            List<String> matches = textProcessor.findMatches(
                                    outputContent,
                                    regexPattern,
                                    flags
                            );

                            // Append matches to analysis results
                            final List<String> finalMatches = matches;
                            Platform.runLater(() -> {
                                analysisResults.append("\n--- Matches in " + fileName + " ---\n");
                                if (finalMatches.isEmpty()) {
                                    analysisResults.append("No matches found\n");
                                } else {
                                    for (String match : finalMatches) {
                                        analysisResults.append("- ").append(match).append("\n");
                                    }
                                    analysisResults.append("Total matches: ").append(finalMatches.size()).append("\n");
                                }
                            });
                        }

                        if (highlightMatches) {
                            outputContent = textProcessor.highlightMatches(
                                    outputContent,
                                    regexPattern,
                                    highlightPrefix,
                                    highlightSuffix,
                                    flags
                            );
                        }

                        if (replaceMatches) {
                            outputContent = textProcessor.replaceAll(
                                    outputContent,
                                    regexPattern,
                                    replacement,
                                    flags
                            );
                        }
                    } catch (PatternSyntaxException e) {
                        Platform.runLater(() -> {
                            analysisResults.append("\nError in " + fileName + ": Invalid regex pattern: " + e.getMessage() + "\n");
                        });
                        continue;
                    }
                }

                // Perform text analysis if needed
                Map<String, Long> wordFreq = null;
                String summaryText = null;

                if (performAnalysis) {
                    if (wordFrequency) {
                        wordFreq = dataAnalyzer.wordFrequencyAnalysis(content, minWordLength);
                    }

                    if (summarization) {
                        summaryText = DataAnalyzer.summarizeText(content, maxSentences);
                    }

                    // Format and store analysis results
                    final Map<String, Long> finalWordFreq = wordFreq;
                    final String finalSummary = summaryText;

                    Platform.runLater(() -> {
                        analysisResults.append("\n\n--- Analysis Results for: " + fileName + " ---\n");

                        // Add word frequency results
                        if (finalWordFreq != null && !finalWordFreq.isEmpty()) {
                            analysisResults.append("\nWord Frequency Analysis:\n");
                            analysisResults.append(String.format("%-20s %s\n", "Word", "Frequency"));
                            analysisResults.append("----------------------------------------\n");

                            // Sort by frequency (descending)
                            List<Map.Entry<String, Long>> sortedEntries = new ArrayList<>(finalWordFreq.entrySet());
                            sortedEntries.sort(Map.Entry.<String, Long>comparingByValue().reversed());

                            // Display top results (limit to avoid overwhelming output)
                            int count = 0;
                            for (Map.Entry<String, Long> entry : sortedEntries) {
                                analysisResults.append(String.format("%-20s %d\n", entry.getKey(), entry.getValue()));
                                if (++count >= 20) {  // Limit to top 20 words
                                    analysisResults.append("... (showing top 20 results)\n");
                                    break;
                                }
                            }
                        }

                        // Add summarization results
                        if (finalSummary != null) {
                            analysisResults.append("\nText Summary:\n\n");
                            analysisResults.append(finalSummary).append("\n");
                        }
                    });
                }

                // Save modified content if regex operations were performed
                if (performRegexOps) {
                    // Create output file in the output directory
                    String outputFileName = fileName;
                    if (outputFileName.contains(".")) {
                        // Insert processing indicator before the extension
                        int dotIndex = outputFileName.lastIndexOf(".");
                        outputFileName = outputFileName.substring(0, dotIndex) +
                                ".processed" +
                                outputFileName.substring(dotIndex);
                    } else {
                        // No extension, just append
                        outputFileName += ".processed";
                    }

                    File outputFile = new File(outputDir, outputFileName);
                    saveToFile(outputFile, outputContent);
                }

                // Update preview of first processed file
                if (processedFiles == 0) {
                    final String preview = outputContent;
                    Platform.runLater(() -> {
                        fileContentPreviewArea.setText("Preview of first processed file:\n\n" + preview);
                    });
                }

                // Update progress
                processedFiles++;
                final int currentProgress = processedFiles;
                final double progressValue = (double) processedFiles / totalFiles;

                Platform.runLater(() -> {
                    statusLabel.setText("Batch processing: " + currentProgress + "/" + totalFiles +
                            " files (" + currentFile.getName() + ")");
                    progressBar.setProgress(progressValue);

                    // Update the main output area with current analysis results
                    mainOutputTextArea.setText(analysisResults.toString());
                });

            } catch (IOException ex) {
                final String errorMsg = "Error processing file " + file.getName() + ": " + ex.getMessage();
                Platform.runLater(() -> {
                    analysisResults.append("\nError: " + errorMsg + "\n");
                    mainOutputTextArea.setText(analysisResults.toString());
                    statusLabel.setText(errorMsg);
                });
            }
        }

        // Final update on completion
        int finalProcessedFiles = processedFiles;
        Platform.runLater(() -> {
            statusLabel.setText("Batch processing completed: " + finalProcessedFiles + "/" + totalFiles + " files");
            progressBar.setProgress(1.0);
            showBatchSummary(finalProcessedFiles, totalFiles);
        });
    }



    // Fix the processBatchAnalysis method
    private void processBatchAnalysis(List<File> files, int minLength, int maxSentences) {
        if (files == null || files.isEmpty()) {
            showError("Batch Analysis Error", "No files selected for batch analysis");
            return;
        }

        new Thread(() -> {
            int totalFiles = files.size();
            int processedFiles = 0;

            // Update the UI
            Platform.runLater(() -> {
                statusLabel.setText("Batch analysis started: 0/" + totalFiles + " files");
                progressBar.setProgress(0);
                progressBar.setVisible(true);
                mainOutputTextArea.clear();
            });

            for (File file : files) {
                try {
                    String content = fileProcessor.readFile(file);
                    final String fileName = file.getName();

                    // Perform analysis and append results to output
                    final Map<String, Long> wordFreq = wordFrequencyCheckBox.isSelected() ?
                            dataAnalyzer.wordFrequencyAnalysis(content, minLength) : null;

                    final String summary = summarizationCheckBox.isSelected() ?
                            DataAnalyzer.summarizeText(content, maxSentences) : null;

                    Platform.runLater(() -> {
                        mainOutputTextArea.appendText("\n\n--- Results for: " + fileName + " ---\n");

                        // Add word frequency results
                        if (wordFreq != null && !wordFreq.isEmpty()) {
                            mainOutputTextArea.appendText("\nWord Frequency Analysis:\n");
                            mainOutputTextArea.appendText(String.format("%-20s %s\n", "Word", "Frequency"));
                            mainOutputTextArea.appendText("----------------------------------------\n");

                            for (Map.Entry<String, Long> entry : wordFreq.entrySet()) {
                                mainOutputTextArea.appendText(String.format("%-20s %d\n",
                                        entry.getKey(), entry.getValue()));
                            }
                        }

                        // Add summarization results
                        if (summary != null) {
                            mainOutputTextArea.appendText("\nText Summary:\n\n");
                            mainOutputTextArea.appendText(summary);
                        }
                    });

                    // Update progress
                    processedFiles++;
                    final int currentProgress = processedFiles;
                    final double progressValue = (double) processedFiles / totalFiles;

                    Platform.runLater(() -> {
                        statusLabel.setText("Batch analysis: " + currentProgress + "/" + totalFiles +
                                " files (" + fileName + ")");
                        progressBar.setProgress(progressValue);
                    });

                } catch (IOException ex) {
                    final String errorMsg = "Error analyzing file " + file.getName() + ": " + ex.getMessage();
                    Platform.runLater(() -> {
                        mainOutputTextArea.appendText("\nError: " + errorMsg + "\n");
                        statusLabel.setText(errorMsg);
                    });
                }
            }

            // Final update on completion
            int finalProcessedFiles = processedFiles;
            Platform.runLater(() -> {
                statusLabel.setText("Batch analysis completed: " + finalProcessedFiles + "/" + totalFiles + " files");
                progressBar.setProgress(1.0);
            });
        }).start();
    }

    // Add this method to show batch summary (referenced but not implemented)
    private void showBatchSummary(int processedFiles, int totalFiles) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Batch Processing Complete");
        alert.setHeaderText(null);
        alert.setContentText("Successfully processed " + processedFiles + " out of " + totalFiles + " files.");
        alert.showAndWait();
    }



    public static void main(String[] args) {
        launch(args);
    }

}