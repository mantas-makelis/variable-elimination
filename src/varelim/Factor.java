package varelim;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.stream.Stream;

/**
 * Represents the factor in variable elimination algorithm.
 *
 * @author Mantas Makelis, David Leeftink
 */
public class Factor {

    private ArrayList<Variable> variables;
    private ArrayList<ProbRow> probabilities;

    /**
     * Constructor of the factor.
     * NOTE: It eliminates the unnecessary probabilities of the observed variables.
     *
     * @param variable the variable to which the factor belongs
     * @param probTable the probability table of the variable
     */
    public Factor(Variable variable, Table probTable) {
        variables = new ArrayList<>();
        variables.add(variable);
        variables.addAll(variable.getParents());

        ArrayList<Variable> observedToRemove = new ArrayList<>();
        ArrayList<ProbRow> probsToRemove = new ArrayList<>();
        for (int i = 0; i < variables.size(); i++) {
            if (variables.get(i).isObserved()) {
                for (ProbRow row : probTable.getTable()) {
                    if (!variables.get(i).getObservedValue().equals(row.getValues().get(i))) {
                        probsToRemove.add(row);
                    } else {
                        row.getValues().remove(i);
                    }
                }
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
        probabilities = probTable.getTable();
    }

    /**
     * Constructor of the factor from other factors with the variable which must be eliminated in progress
     *
     * @param factors a list of factors which contain the eliminate variable which must be merged
     * @param eliminate a variable which will be eliminated in the process
     */
    public Factor(ArrayList<Factor> factors, Variable eliminate) {
        Factor factor = null;
        LinkedList<Factor> factorList = new LinkedList<>(factors);
        while (!factorList.isEmpty()) {
            if (factor == null) {
                factor = new Factor(factorList.pop(), factorList.pop(), eliminate);
            } else {
                factor = new Factor(factor, factorList.pop(), eliminate);
            }
        }
        if (factor != null) {
            int probsSize = 1;
            for (Variable var : factor.variables) {
                if (!var.equals(eliminate)) {
                    probsSize *= var.getNumberOfValues();
                }
            }
            int index = factor.variables.indexOf(eliminate);

            /* TODO: Below lies the problem of summing the variable, I am not exactly sure right now how to solve it.
             (Check slides .pdf Bayesian Networks 2, page 4, bottom left, Factor marginalization) */

            ArrayList<ProbRow> probs = new ArrayList<>();
            for (int i = 0; i < probsSize; i++) {
                double prob = 0;
                ArrayList<String> values = new ArrayList<>();
                for (String value : eliminate.getValues()) {
                    for (ProbRow row : factor.probabilities) {
                        if (value.equals(row.getValues().get(index))) {
                            ArrayList<String> vals = new ArrayList<>();
                            for (String val : row.getValues()) {
                                if (!val.equals(value)) {
                                    vals.add(val);
                                }
                            }
                            if (values.equals(vals)) {
                                prob += row.getProb();
                            }
                        }
                    }
                }
                probs.add(new ProbRow(values, prob));
            }

            ArrayList<Variable> vars = factor.getVariables();
            vars.remove(eliminate);
            variables = vars;
            probabilities = probs;
        }
    }

    private Factor(Factor factor1, Factor factor2, Variable eliminate) {
        int index1 = factor1.variables.indexOf(eliminate);
        int index2 = factor2.variables.indexOf(eliminate);
        ArrayList<ProbRow> probs1 = factor1.probabilities;
        ArrayList<ProbRow> probs2 = factor2.probabilities;
        ArrayList<ProbRow> combinedProbs = new ArrayList<>();

        for (ProbRow row1 : probs1) {
            ArrayList<String> values1 = row1.getValues();
            for (ProbRow row2 : probs2) {
                ArrayList<String> values2 = row2.getValues();
                if (values1.get(index1).equals(values2.get(index2))) {
                    ArrayList<String> values = new ArrayList<>(values1);
                    for (int i = 0; i < values2.size(); i++) {
                        if (i != index2) {
                            values.add(values2.get(i));
                        }
                    }
                    double prob = row1.getProb() * row2.getProb();
                    combinedProbs.add(new ProbRow(values, prob));
                }
            }
        }

        HashSet<Variable> combinedVars = new HashSet<>();
        Stream.of(factor1.variables, factor2.variables).forEach(combinedVars::addAll);

        variables = new ArrayList<>(combinedVars);
        probabilities = combinedProbs;
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
