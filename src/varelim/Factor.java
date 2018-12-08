package varelim;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.stream.Stream;

/**
 * Represents the factor in variable elimination algorithm and is the part of it.
 * The class contains few constructors. One constructor initialises factors, others merge a list of factors into one.
 *
 * @author Mantas Makelis, David Leeftink
 */
public class Factor {

    private ArrayList<Variable> variables; // Variables that are contained in the factor
    private ArrayList<ProbRow> probabilities; // The probability table of this factor

    /**
     * This constructor is used to initialise the factor out of a variable.
     * NOTE: It also eliminates the unnecessary probabilities of the observed variables.
     *
     * @param variable the variable to which the factor belongs
     * @param probTable the probability table of the variable
     */
    public Factor(Variable variable, Table probTable) {
        // Add all variables that the factor contains
        variables = new ArrayList<>();
        variables.add(variable);
        variables.addAll(variable.getParents());

        // Remove the observed variables and some of their probability rows
        ArrayList<Variable> observedToRemove = new ArrayList<>();
        ArrayList<ProbRow> probsToRemove = new ArrayList<>();
        for (int i = 0; i < variables.size(); i++) {
            // Run only for observed variables
            if (variables.get(i).isObserved()) {
                // Search for unobserved probabilities
                for (ProbRow row : probTable.getTable()) {
                    if (!variables.get(i).getObservedValue().equals(row.getValues().get(i))) {
                        // If value of the observed variable in the probability table is different than observed - add to remove list.
                        probsToRemove.add(row);
                    } else {
                        // Remove the value (NOT probability itself) from the probability table e.g. remove not A
                        row.getValues().remove(i);
                    }
                }
                // Also, if the observed variable is the base of THIS factor, add ir to remove from the factor variables
                if (!variables.get(i).equals(variable)) {
                    observedToRemove.add(variables.get(i));
                }
            }
        }
        for (Variable var : observedToRemove) {
            variables.remove(var);
        }
        for (ProbRow row : probsToRemove) {
            probTable.getTable().remove(row);
        }
        // Add probability table to the factor
        probabilities = probTable.getTable();
    }

    /**
     * Constructor of the factor from other factors with the variable which must be eliminated in progress
     * This constructor is only used to merge a number of factors into one.
     *
     * @param factors a list of factors which contain the eliminate variable which must be merged
     * @param eliminate a variable which will be eliminated in the process
     */
    public Factor(ArrayList<Factor> factors, Variable eliminate) {
        // Initialise null factor for future use
        Factor factor = new Factor();
        // Push all given factors into a queue list
        LinkedList<Factor> factorList = new LinkedList<>(factors);
        // Run until all given factors are no longer in the list
        while (!factorList.isEmpty()) {
            if (factor.isNull()) {
                // First time of the loop, pop two factors out and merge them with another constructor
                factor = mergeFactors(factorList.pop(), factorList.pop(), eliminate);
            } else {
                // All the other times of the loop, pop one factor and merge with the previously merged factor
                factor = mergeFactors(factor, factorList.pop(), eliminate);
            }
        }

        // TODO: factor marginalization

        // Retrieve variables from the merged factor
        ArrayList<Variable> vars = factor.getVariables();
        // Remove the eliminated variable
        vars.remove(eliminate);
        // Set the variable and probs to THIS factor
        variables = factor.getVariables();
        probabilities = factor.getProbabilities();
    }

    /**
     * The merger factor constructor which is only used in the constructor of multiple factors.
     * This constructor creates one factor out of two.
     *
     * @param factor1 first factor
     * @param factor2 second factor
     * @param eliminate the variable to eliminate
     */
    private Factor mergeFactors(Factor factor1, Factor factor2, Variable eliminate) {
        if (factor1.isSubFactor(factor2)) {
            return factor1;
        } else if (factor2.isSubFactor(factor1)) {
            return factor2;
        } else {
            // Create HashSet (because no duplicates), add variables from both factors
            HashSet<Variable> combinedVars = new HashSet<>();
            Stream.of(factor1.variables, factor2.variables).forEach(combinedVars::addAll);
            // Initialise the array of combined probabilities
            ArrayList<ProbRow> combinedProbs = new ArrayList<>();

            int probsSize = 1;
            for (Variable var : combinedVars){
                probsSize *= var.getNumberOfValues();
            }

            for (int i = 0; i < probsSize; i++) {

            }

            /*// Get indexes (column number) from both factors of the variable which we want to eliminate
            int index1 = factor1.variables.indexOf(eliminate);
            int index2 = factor2.variables.indexOf(eliminate);
            // Retrieve the probabilities of both factors
            ArrayList<ProbRow> probs1 = factor1.probabilities;
            ArrayList<ProbRow> probs2 = factor2.probabilities;
            // Initialise new array which will contain combined probabilities

            // TODO: merge probabilities

            for (ProbRow row1 : probs1) {
                // Get a probabilities row from first factor
                ArrayList<String> values1 = row1.getValues();
                for (ProbRow row2 : probs2) {
                    // Get a probabilities row from second factor
                    ArrayList<String> values2 = row2.getValues();
                    // If both rows of both factors contain elimination variable - merge to new row, now containing only 1 elimination var
                    if (values1.get(index1).equals(values2.get(index2))) {
                        // Initialise array with values of the first factor
                        ArrayList<String> values = new ArrayList<>(values1);
                        // Add all from the second factor except the elimination variable
                        for (int i = 0; i < values2.size(); i++) {
                            if (i != index2) {
                                values.add(values2.get(i));
                            }
                        }
                        // Multiply probabilities and assign it to the row
                        double prob = row1.getProb() * row2.getProb();
                        combinedProbs.add(new ProbRow(values, prob));
                    }
                }
            }*/

            return new Factor(new ArrayList<>(combinedVars), combinedProbs);
        }
    }

    /**
     * Dummy factor constructor.
     */
    private Factor() {
    }

    /**
     * A constructor which sets the variables and probabilities.
     *
     * @param variables variables of the factor
     * @param probabilities probabilities of the factor
     */
    private Factor(ArrayList<Variable> variables, ArrayList<ProbRow> probabilities) {
        this.variables = variables;
        this.probabilities = probabilities;
    }

    /**
     * Check if the given factor is a sub-factor.
     * @param factor the factor which potentially might be a sub-factor
     * @return true if it is a sub-factor, otherwise, false
     */
    private boolean isSubFactor(Factor factor) {
        for (Variable var : factor.getVariables()){
            if (!variables.contains(var)){
                return false;
            }
        }
        return true;
    }

    /**
     * A check if the factor is empty.
     *
     * @return true if not empty, otherwise, false
     */
    private boolean isNull() {
        return probabilities == null && variables == null;
    }

    /**
     * Getter for the variables of the factor.
     *
     * @return variables
     */
    private ArrayList<Variable> getVariables() {
        return variables;
    }

    /**
     * Getter for probabilities of the factor.
     *
     * @return probabilities
     */
    private ArrayList<ProbRow> getProbabilities() {
        return probabilities;
    }

    /**
     * A check to see if the supplied variable is in the factor.
     *
     * @param var the variable for which to look
     * @return true if the factor contains the variable, otherwise, false
     */
    public boolean containsVariable(Variable var) {
        return variables.contains(var);
    }

    /**
     * Converts the factor probabilities table to a string.
     *
     * @return a string of the table
     */
    public String stringifyProbs(Variable query) {
        return new Table(query, probabilities).toString();
    }
}
