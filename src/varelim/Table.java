package varelim;

import java.util.ArrayList;

/**
 * Class to represent a probability table consisting of probability rows.
 *
 * @author Marcel de Korte, Moira Berens, Djamari Oetringer, Abdullahi Ali, Leonieke van den Bulk
 * @co-author/editor Mantas Makelis, David Leeftink
 */

public class Table {

    private Variable variable;
    private ArrayList<ProbRow> table;

    /**
     * Constructor of the class.
     *
     * @param variable, variable belonging to the current probability table.
     * @param table, table made out of probability rows (ProbRows).
     */
    public Table(Variable variable, ArrayList<ProbRow> table) {
        this.variable = variable;
        this.table = table;
    }

    /**
     * Returns the size of the Table (amount of probability rows).
     *
     * @return amount of rows in the table as an int.
     */
    public int size() {
        return table.size();
    }

    /**
     * Transform table to string.
     */
    public String toString() {
        StringBuilder tableString = new StringBuilder(variable.getName() + " | ");
        for (int i = 0; i < variable.getNrOfParents(); i++) {
            tableString.append(variable.getParents().get(i).getName());
            if (!(i == variable.getNrOfParents() - 1)) {
                tableString.append(", ");
            }
        }
        for (ProbRow row : table) {
            tableString.append("\n").append(row.toString());
        }
        return tableString.toString();
    }

    /**
     * Gets the i'th element from the ArrayList of ProbRows.
     *
     * @param i index as an int.
     * @return i'th ProbRow in Table.
     */
    public ProbRow get(int i) {
        return table.get(i);
    }

    /**
     * Getter of the table made out of ProbRows
     *
     * @return table as an ArrayList of ProbRows.
     */
    public ArrayList<ProbRow> getTable() {
        return table;
    }

    /**
     * Getter of the variable that belongs to the probability table.
     *
     * @return the variable.
     */
    public Variable getVariable() {
        return variable;
    }

    /**
     * Getter of the parents that belong to the node of the probability table.
     *
     * @return the parents as an ArrayList of Variables.
     */
    public ArrayList<Variable> getParents() {
        return variable.getParents();
    }
}
