package flashcards;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class Main {

    private static Scanner scanner = new Scanner(System.in);
    private static Map<String, Flashcard> flashcards = new HashMap<>();
    private static Random random = new Random();
    private static List<String> toLog = new ArrayList<>();
    private static List<Flashcard> hardestFlashcards = new ArrayList<>();
    private static String exportFilename = null;

    public static void main(String[] args) {
        if (args.length == 2) {
            if ("-import".equals(args[0])) {
                importCards(args[1]);
            } else if ("-export".equals(args[0])) {
                exportFilename = args[1];
            }
        } else if (args.length == 4) {
            if ("-import".equals(args[0])) {
                importCards(args[1]);
                exportFilename = args[3];
            } else if ("-export".equals(args[0])) {
                exportFilename = args[1];
                importCards(args[3]);
            }
        }
        Mode mode = Mode.START;
        while (mode != Mode.EXIT) {
            printAndLog("Input the action (add, remove, import, " +
                    "export, ask, exit, log, hardest card, reset stats):");
            mode = Mode.getModeByInput(readAndLog());
            switch (mode) {
                case ADD:
                    add();
                    break;
                case REMOVE:
                    remove();
                    break;
                case IMPORT:
                    importCards();
                    break;
                case EXPORT:
                    exportCards();
                    break;
                case ASK:
                    ask();
                    break;
                case LOG:
                    log();
                    break;
                case HARDEST_CARD:
                    hardest_card();
                    break;
                case RESET_STATS:
                    reset_stats();
                    break;
                case EXIT:
                    exit();
                    break;
            }
        }
    }

    public static void add() {
        printAndLog("The card:");
        String term = readAndLog();
        if (flashcards.containsKey(term)) {
            printAndLog("The card \"" + term + "\" already exists.\n");
            return;
        }
        printAndLog("The definition of the card:");
        String definition = readAndLog();
        if (keyToValue(definition).isPresent()) {
            printAndLog("The definition \"" + definition + "\" already exists.\n");
            return;
        }
        flashcards.put(term, new Flashcard(term, definition));
        printAndLog("The pair (\"" + term + "\":\"" + definition + "\") has been added.\n");
    }

    public static void remove() {
        printAndLog("Which card?");
        String key = readAndLog();
        if (!flashcards.containsKey(key)) {
            printAndLog("Can't remove \"" + key + "\": there is no such card.\n");
            return;
        }
        flashcards.remove(key);
        printAndLog("The card has been removed.\n");
    }

    public static void exit() {
        printAndLog("Bye bye!\n");
        if (exportFilename != null) {
            exportCards(exportFilename);
        }

    }

    public static void importCards(String filename) {
        File file = new File(filename);
        int counter = 0;
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNext()) {
                String[] entry = scanner.nextLine().split("\\t");
                flashcards.put(entry[0], new Flashcard(entry[0], entry[1], Integer.parseInt(entry[2])));
                counter += 1;
            }
        } catch (FileNotFoundException ignored) {}
        printAndLog(counter + " cards have been loaded.\n");
    }

    public static void exportCards(String filename) {
        File file = new File(filename);
        int counter = 0;
        try (PrintWriter printWriter = new PrintWriter(file)) {
            for (Flashcard flashcard : flashcards.values()) {
                printWriter.println(flashcard.getTerm() +
                        "\t" + flashcard.getDefinition() +
                        "\t" + flashcard.getMistake());
                counter += 1;
            }
        } catch (IOException ignored) {}
        printAndLog(counter + " cards have been saved.\n");
    }

    public static void importCards() {
        printAndLog("File name:");
        File file = new File(readAndLog());
        int counter = 0;
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNext()) {
                String[] entry = scanner.nextLine().split("\\t");
                flashcards.put(entry[0], new Flashcard(entry[0], entry[1], Integer.parseInt(entry[2])));
                counter += 1;
            }
        } catch (FileNotFoundException e) {
            printAndLog("File not found\n");
            return;
        }
        printAndLog(counter + " cards have been loaded.\n");
    }

    public static void exportCards() {
        printAndLog("File name:");
        File file = new File(readAndLog());
        int counter = 0;
        try (PrintWriter printWriter = new PrintWriter(file)) {
            for (Flashcard flashcard : flashcards.values()) {
                printWriter.println(flashcard.getTerm() +
                        "\t" + flashcard.getDefinition() +
                        "\t" + flashcard.getMistake());
                counter += 1;
            }
        } catch (IOException e) {
            return;
        }
        printAndLog(counter + " cards have been saved.\n");
    }

    public static void ask() {
        printAndLog("How many times to ask?");
        List<String> keys = new ArrayList<>(flashcards.keySet());
        int n = Integer.parseInt(readAndLog());
        for (int i = 0; i < n; i++) {
            int randomIndex = random.nextInt(flashcards.size());
            String key = keys.get(randomIndex);
            printAndLog("Print the definition of \"" + key + "\":");
            String answer = readAndLog();
            if (answer.equals(flashcards.get(key).getDefinition())) {
                printAndLog("Correct!\n");
            } else if (keyToValue(answer).isPresent()) {
                incrementFlashcardMistake(key);
                String keyToValue = keyToValue(answer).get().getKey();
                printAndLog("Wrong. The right answer is \"" +
                        flashcards.get(key).getDefinition() +
                        "\", but your definition is correct for \"" +
                        keyToValue + "\".\n");
            } else {
                incrementFlashcardMistake(key);
                printAndLog("Wrong. The right answer is \"" +
                        flashcards.get(key).getDefinition() +
                        "\".\n");
            }
        }
    }

    public static void hardest_card() {
        getHardestFlashcards();
        if (hardestFlashcards.isEmpty()) {
            printAndLog("There are no cards with errors.\n");
        } else if (hardestFlashcards.size() == 1) {
            Flashcard toPrint = hardestFlashcards.get(0);
            printAndLog("The hardest card is \"" +
                    toPrint.getTerm() +
                    "\". You have " +
                    toPrint.getMistake() +
                    " errors answering it\n");
        } else {
            StringBuilder stringBuilder= new StringBuilder();
            int errorCount = 0;
            for (int i = 0; i < hardestFlashcards.size(); i++) {
                if (i != hardestFlashcards.size() - 1) {
                    stringBuilder.append("\"").append(hardestFlashcards.get(i).getTerm()).append("\", ");
                } else {
                    stringBuilder.append("\"").append(hardestFlashcards.get(i).getTerm()).append("\"");
                }
                errorCount += hardestFlashcards.get(i).getMistake();
            }
            printAndLog("The hardest cards are " + stringBuilder.toString() +
                    ". You have " + errorCount + " errors answering them\n");
        }
    }

    public static void reset_stats() {
        flashcards.forEach((x, y) -> y.resetMistake());
        hardestFlashcards.clear();
        printAndLog("Card statistics have been reset.\n");
    }

    public static void log() {
        printAndLog("File name:");
        File file = new File(readAndLog());
        try (PrintWriter printWriter = new PrintWriter(new FileOutputStream(file, true))) {
            for (String line : toLog) {
                printWriter.println(line);
            }
        } catch (IOException ignored) {}
        printAndLog("The log has been saved\n");
    }

    public static Optional<Map.Entry<String, Flashcard>> keyToValue(String definition) {
        return flashcards.entrySet().stream().filter(x -> x.getValue().getDefinition().equals(definition)).findFirst();
    }

    public static void getHardestFlashcards() {
        List<Flashcard> hardest = flashcards.values().stream()
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());
        hardestFlashcards = hardest.stream()
                .filter(x -> x.getMistake() != 0)
                .filter(x -> x.getMistake() == hardest.get(0).getMistake())
                .collect(Collectors.toList());
    }

    public static void printAndLog(String text) {
        System.out.println(text);
        toLog.add(text);
    }

    public static String readAndLog() {
        String input = scanner.nextLine();
        toLog.add("> " + input);
        return input;
    }

    public static void incrementFlashcardMistake(String key) {
        flashcards.get(key).incrementMistake();
    }
}
