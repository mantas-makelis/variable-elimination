package varelim;

import java.util.*;

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
     * 2. Fix order
     * 3. Multiply factors in order
     * 4. Marginalize factors
     * 5. Normalize results
     *
     * @param query the variable for which the probability needs to be determined
     * @param vars a list of all the variables in the Bayesian network
     * @param probs a list of the probability tables for each variable
     */
    public void runElimination(Variable query, ArrayList<Variable> vars, ArrayList<Table> probs) {
        ui.printProductFormula(vars);
        // Initialise factors according to all variables
        ArrayList<Factor> factors = factorize(vars, probs);

        // Create the order of elimination according to the number of parents
        Queue<Variable> elimOrder = compriseOrder(query, vars, factors);
        ui.printEliminationOrder(new PriorityQueue<>(elimOrder));

        while (!elimOrder.isEmpty()) {
            Variable eliminate = elimOrder.poll();

            // Retrieve factors which contain the popped variable
            ArrayList<Factor> concerningFactors = getFactorsContainingEliminate(eliminate, factors);
            ui.printMergingFactors(concerningFactors, eliminate);

            // Eliminate the variable which is contained in at least 2 factors
            if (concerningFactors.size() > 1) {
                Factor mergedFactor = new Factor(concerningFactors, eliminate);
                factors.add(mergedFactor);
            }
            // Any factor concerning 1 variable results in (1,1) probability.
            ui.printFactorFormula(factors, false);
        }
        Factor finalFactor;

        // In case the query is the ancestor of observed variables.
        if (factors.size() > 1) {
            ui.printLastFactorMerge(factors);
            finalFactor = new Factor(factors, null);
        }
        else {
            finalFactor = factors.get(0);
        }

        // Gather results
        finalFactor = normalize(finalFactor);
        ui.printQueryAnswer(finalFactor.toString());

    }

    /**
     * Normalize the final factor by the formula : factor / (sum of probabilities of the factor)
     *
     * @param finalFactor
     * @return
     */
    private Factor normalize(Factor finalFactor) {
        // Sum up all the probabilities
        double sumProb = 0;
        for (ProbRow row : finalFactor.getProbabilities()) {
            sumProb += row.getProb();
        }
        for (ProbRow probability : finalFactor.getProbabilities()) {
            // Normalize results, round to 5 digits.
            probability.setProb(Math.round((probability.getProb() / sumProb) * 100000.0) / 100000.0);
        }
        return finalFactor;
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
            Factor factor = new Factor(var, getProb(var, probs));
            factors.add(factor);
        }
        ArrayList<Factor> fullyObserved = getFullyObserved(factors);
        ui.printFactorFormula(fullyObserved, true);
        return fullyObserved;
    }

    /**
     * Gets the fully observed factors removed.
     * Remove factors with only observed.
     *
     * @param factors factor list
     * @return a list of factors
     */
    private ArrayList<Factor> getFullyObserved(ArrayList<Factor> factors) {
        ArrayList<Factor> updatedFactors = new ArrayList<>();
        for (Factor factor : factors) {
            boolean remove = true;
            for (Variable variable : factor.getVariables()) {
                if (!variable.isObserved()) {
                    remove = false;
                }
            }
            if (!remove) {
                updatedFactors.add(factor);
            }
        }
        return updatedFactors;
    }

    /**
     * Comprises the order in which to eliminate the variables.
     * Consists of least-arcs incoming, fewest factors and random.
     *
     * @param query the variable for which the probability needs to be determined
     * @param vars a list of all the variables in the Bayesian network.
     * @return the order of elimination
     */
    private Queue<Variable> compriseOrder(Variable query, ArrayList<Variable> vars, ArrayList<Factor> factors) {
        // Initialise the priority queue which compares members by the number of their parents
        PriorityQueue<Variable> order = new PriorityQueue<>(Comparator.comparing(Variable::getNrOfParents));

        // Add variables which are NOT observed and is not a query
        for (Variable var : vars) {
            if (!var.isObserved() && !var.equals(query)) {
                order.add(var);
            }
        }
        return order;

       /* if (ui.getHeuristic() == "least-incoming") {
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

        else if (ui.getHeuristic() == "fewest-factors"){
            // fewest factors
            for (Variable var : vars){
                int count = 0;
                for (Factor factor : factors){
                    if (factor.getVariables().contains(var)){
                        count++;
                    }
                }
                var.setNrFactors(count);
            }
            PriorityQueue<Variable> order = new PriorityQueue<>(Comparator.comparing(Variable::getNrFactors));
            return order;
        }

        else{
            // Initialise the priority queue with random order
            System.out.println(vars);
            Collections.shuffle(vars);
            System.out.println(vars);
            LinkedList<Variable> order = new LinkedList<>(vars);
            System.out.println(order);
            return order;
        }*/
    }

    /**
     * Retrieves the corresponding probability table for a variable.
     *
     * @param var the variable for which to retrieve the table
     * @param probs a list of the probability tables for each variable
     * @return the corresponding probability table
     */
    public Table getProb(Variable var, ArrayList<Table> probs) {
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
    public ArrayList<Factor> getFactorsContainingEliminate(Variable var, ArrayList<Factor> factors) {
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
