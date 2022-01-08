package flashcards;

import java.util.Arrays;

public enum Mode {

    START("start"),
    ADD("add"),
    REMOVE("remove"),
    IMPORT("import"),
    EXPORT("export"),
    ASK("ask"),
    EXIT("exit"),
    LOG("log"),
    HARDEST_CARD("hardest card"),
    RESET_STATS("reset stats");

    String mode;

    Mode(String mode) {
        this.mode = mode;
    }

    public static Mode getModeByInput(String mode) {
        return Arrays.stream(Mode.values())
                .filter(x -> x.mode.equals(mode))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}
