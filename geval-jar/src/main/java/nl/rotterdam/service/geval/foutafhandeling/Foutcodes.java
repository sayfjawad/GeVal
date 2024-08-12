package nl.rotterdam.service.geval.foutafhandeling;

/**
 * Zie 'errormessages.properties' voor de bijbehorende omschrijvingen
 */
public enum Foutcodes {
    INVALIDE_INPUT_FOUT(400),
    TIMEOUT_FOUT(504),
    INTERNE_FOUT(500);

    private int httpStatus;

    Foutcodes(final int httpStatus) {
        this.httpStatus = httpStatus;
    }

    public int getHttpStatus() {
        return this.httpStatus;
    }
}