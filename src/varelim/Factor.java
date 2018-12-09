package varelim;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.PriorityQueue;

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

        ArrayList<Variable> varToRemove = new ArrayList<>();
        ArrayList<Integer> colToRemove = new ArrayList<>();
        for (int i = 0; i < variables.size(); i++) {
            if (variables.get(i).isObserved()) {
                varToRemove.add(variables.get(i));
                colToRemove.add(i);
                String value = variables.get(i).getObservedValue();
                ArrayList<ProbRow> rowToRemove = new ArrayList<>();
                for (ProbRow row : probTable.getTable()) {
                    if (!row.getValues().get(i).equals(value)) {
                        rowToRemove.add(row);
                    }
                }
                for (ProbRow row : rowToRemove) {
                    probTable.getTable().remove(row);
                }
            } else {
                probabilities = probTable.getTable();
            }
        }
        for (int i = 0; i < colToRemove.size(); i++) {
            if (variables.size() > 1 && !variable.equals(varToRemove.get(i))) {
                variables.remove(varToRemove.get(i));
                for (ProbRow row : probTable.getTable()) {
                    int index = colToRemove.get(i);
                    row.getValues().remove(index);
                }
            }
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
        if (eliminate != null) {
            // Push all given factors into a queue list
            PriorityQueue<Factor> factorQueue = new PriorityQueue<>(Comparator.comparing(Factor::getNoOfVariables));
            factorQueue.addAll(factors);
            // Run until all given factors are no longer in the list
            while (!factorQueue.isEmpty()) {
                if (factor.isNull()) {
                    // First time of the loop, pop two factors out and merge them with another constructor
                    factor = mergeFactors(factorQueue.poll(), factorQueue.poll(), eliminate);
                } else {
                    // All the other times of the loop, pop one factor and merge with the previously merged factor
                    factor = mergeFactors(factor, factorQueue.poll(), eliminate);
                }
            }
            int tableSize = 1;
            for (Variable var : factor.getVariables()) {
                if (!var.equals(eliminate) && !var.isObserved()) {
                    tableSize *= var.getNumberOfValues();
                }
            }

            ArrayList<ProbRow> finalTable = new ArrayList<>();
            ArrayList<ArrayList<String>> usedExamples = new ArrayList<>();

            int index = factor.variables.indexOf(eliminate);

            for (ProbRow example : factor.probabilities) {
                ArrayList<String> exampleValues = example.getValues();
                if (!usedExamples.contains(exampleValues)){
                    ArrayList<ArrayList<String>> variations = getPossibleRows(index, eliminate, example.getValues());
                    usedExamples.addAll(variations);
                    ProbRow variation = new ProbRow(variations.get(0), 0);
                    for (ProbRow row : factor.probabilities) {
                        ArrayList<String> dummy = row.getValues();
                        if (variations.contains(dummy)) {
                            variation.setProb(variation.getProb() + row.getProb());
                        }
                    }
                    variation.getValues().remove(index);
                    finalTable.add(variation);
                    if (finalTable.size() == tableSize) {
                        break;
                    }
                }
            }

            // Retrieve variables from the merged factor
            ArrayList<Variable> vars = factor.getVariables();
            // Remove the eliminated variable
            vars.remove(eliminate);
            // Set the variable and probs to THIS factor
            variables = vars;
            probabilities = finalTable;
        } else {
            // This part of the algorithm is called only when the elimination order is empty but the factor list contains more than one.
            // This can happen when the observed variables are the parents or distant parents of the queried variable.
            Variable query = new Variable();
            for (Factor mainFactor : factors) {
                if (mainFactor.getVariables().size() == 1) {
                    query = mainFactor.getVariables().get(0);
                    factor = mainFactor;
                    break;
                }
            }
            factors.remove(factor);
            LinkedList<Factor> eliminateList = new LinkedList<>(factors);
            ArrayList<Variable> varsToRemove = new ArrayList<>();

            while (!eliminateList.isEmpty()) {
                Factor factorToMerge = eliminateList.poll();
                for (Variable var : factorToMerge.getVariables()) {
                    if (!var.equals(query)) {
                        varsToRemove.add(var);
                        break;
                    }
                }
                factor = mergeFactors(factor, factorToMerge, query);
            }
            for (Variable var : varsToRemove) {
                factor.variables.remove(var);
            }
            variables = factor.getVariables();
            probabilities = factor.getProbabilities();
        }
    }

    private ArrayList<ArrayList<String>> getPossibleRows(int index, Variable eliminate, ArrayList<String> example) {
        ArrayList<ArrayList<String>> values = new ArrayList<>();
        for (String value : eliminate.getValues()) {
            ArrayList<String> newExample = (ArrayList<String>) example.clone();
            newExample.set(index, value);
            values.add(newExample);
        }
        return values;
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
        // Initialise the array of combined probabilities
        ArrayList<ProbRow> combinedProbs = new ArrayList<>();

        int index1 = factor1.variables.indexOf(eliminate);
        int index2 = factor2.variables.indexOf(eliminate);
        ArrayList<ProbRow> probs1 = factor1.probabilities;
        ArrayList<ProbRow> probs2 = factor2.probabilities;

        for (ProbRow row1 : probs1) {
            for (ProbRow row2 : probs2) {
                if (row1.getValues().get(index1).equals(row2.getValues().get(index2))) {
                    double prob = row1.getProb() * row2.getProb();
                    ProbRow row;
                    if (probs1.size() > probs2.size()) {
                        row = new ProbRow(row1.getValues(), prob);
                    } else {
                        row = new ProbRow(row2.getValues(), prob);
                    }
                    combinedProbs.add(row);
                }
            }
        }

        ArrayList<Variable> finalVars = factor1.variables.size() < factor2.variables.size() ? factor2.variables : factor1.variables;
        Factor merged = new Factor(finalVars, combinedProbs);
        return merged;
    }

    /**
     * Dummy constructor
     */
    public Factor() {
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
    public ArrayList<Variable> getVariables() {
        return variables;
    }

    /**
     * Getter for probabilities of the factor.
     *
     * @return probabilities
     */
    public ArrayList<ProbRow> getProbabilities() {
        return probabilities;
    }

    /**
     * Gets variable count in the factor.
     *
     * @return count of variables
     */
    public int getNoOfVariables() {
        return variables.size();
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
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n").append(variables.get(0).getName()).append("\n");
        for (ProbRow pr : probabilities) {
            sb.append(pr.getValues()).append(" | ").append(pr.getProb()).append("\n");
        }
        return sb.toString();
    }
}
