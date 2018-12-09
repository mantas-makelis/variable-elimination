package varelim;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * Represents the variable elimination algorithm.
 *
 * @author Mantas Makelis, David Leeftink
 */
public class Algorithm {

    private UserInterface ui;

    public Algorithm(UserInterface ui) {
        this.ui = ui;
    }

    /**
     * Runs variable elimination algorithm which makes factors and merges them in defined order until only one factor is left and the
     * probability of the query can be determined.
     * The order of the algorithm:
     * 1. Collect all factors
     * 2. Figure out the order (probably in order of lowest parent count)
     * 3. Merge factors in order
     * 4. Print the probabilities of the remaining factor
     *
     * @param query the variable for which the probability needs to be determined
     * @param vars a list of all the variables in the Bayesian network
     * @param probs a list of the probability tables for each variable
     */
    public void runElimination(Variable query, ArrayList<Variable> vars, ArrayList<Table> probs) {
        // Initialise factors according to all variables
        ArrayList<Factor> factors = factorize(vars, probs);
        // Create the order of elimination according to the number of parents
        PriorityQueue<Variable> elimOrder = compriseOrder(query, vars);
        // Start elimination process
        while (!elimOrder.isEmpty()) {
            // Pop the variable with lowest number of parents
            Variable eliminate = elimOrder.poll();
            // Retrieve factors which contain the popped variable
            ArrayList<Factor> concerningFactors = getFactorsContainingEliminate(eliminate, factors);
            // Eliminate the variable which is contained in at least 2 factors
            if (concerningFactors.size() > 1) {
                // Create new factor from the given
                Factor mergedFactor = new Factor(concerningFactors, eliminate);
                // Add new factor to the list
                factors.add(mergedFactor);
            }
            // Else case return variable with (1,1) which can be eliminated right away
            // Repeat until elimination order is empty
        }
        // Print the results
        Factor finalFactor = factors.get(0);
        ui.printQueryAnswer(finalFactor.toString());
    }

    /**
     * Converts all the variables to a list of the factors.
     *
     * @param vars a list of all the variables in the Bayesian network
     * @param probs a list of the probability tables for each variable
     * @return a list containing all the factors
     */
    private ArrayList<Factor> factorize(ArrayList<Variable> vars, ArrayList<Table> probs) {
        ArrayList<Factor> factors = new ArrayList<>();
        // Create factors out of all variables which are NOT observed
        for (Variable var : vars) {
            factors.add(new Factor(var, getProb(var, probs)));
        }
        return factors;
    }

    /**
     * Comprises the order in which to eliminate the variables.
     *
     * @param query the variable for which the probability needs to be determined
     * @param vars a list of all the variables in the Bayesian network.
     * @return the order of elimination
     */
    private PriorityQueue<Variable> compriseOrder(Variable query, ArrayList<Variable> vars) {
        // Initialise the priority queue which compares members by the number of their parents
        PriorityQueue<Variable> order = new PriorityQueue<>(Comparator.comparing(Variable::getNrOfParents));
        // Add variables which are NOT observed and is not a query
        for (Variable var : vars) {
            if (!var.isObserved() && !var.equals(query)) {
                order.add(var);
            }
        }
        return order;
    }

    /**
     * Retrieves the corresponding probability table for a variable.
     *
     * @param var the variable for which to retrieve the table
     * @param probs a list of the probability tables for each variable
     * @return the corresponding probability table
     */
    private Table getProb(Variable var, ArrayList<Table> probs) {
        for (Table prob : probs) {
            if (prob.getVariable().equals(var)) {
                return prob;
            }
        }
        return null;
    }

    /**
     * Gets factors which contain the given variable.
     *
     * @param var the variable for which to look in factors
     * @param factors a list of all the factors
     * @return a list containing only the factors which contain the given variable
     */
    private ArrayList<Factor> getFactorsContainingEliminate(Variable var, ArrayList<Factor> factors) {
        ArrayList<Factor> containing = new ArrayList<>();
        // Add factor to the containing if the factor contains variable to eliminate
        for (Factor factor : factors) {
            if (factor.containsVariable(var)) {
                containing.add(factor);
            }
        }
        // Remove all factors from the factor list that contained the variable to eliminate
        for (Factor factor : containing) {
            factors.remove(factor);
        }
        return containing;
    }
}
