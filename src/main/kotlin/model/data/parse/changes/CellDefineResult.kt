package model.data.parse.changes

enum class CellDefineResult {

    /**
     * Continue cycle.
     * Current cell doesn't contain target content.
     *
     * To old versions: this value is analogue to *-1* code.
     */
    CONTINUE,

    /**
     * Read current cell.
     * It contains target information.
     *
     * To old versions: this value is analogue to *0* code.
     */
    READ,

    /**
     * Break the cycles.
     * Parser found another group declaration while reading targetChangesOfDay.
     *
     * To old versions: this value is analogue to *1* code.
     */
    BREAK
}
