package varelim;

import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Scanner;

/**
 * Class that handles the communication with the user.
 *
 * @author Marcel de Korte, Moira Berens, Djamari Oetringer, Abdullahi Ali, Leonieke van den Bulk
 * @co-author/editor Mantas Makelis, David Leeftink
 */

public class UserInterface {

    private ArrayList<Variable> vs;
    private ArrayList<Table> ps;
    private Variable query = null;
    private ArrayList<Variable> obs = new ArrayList<>();
    private String line;
    private String heuristic;
    private Scanner scan;

    /**
     * Constructor of the user interface.
     *
     * @param vs, the list of variables.
     * @param ps, the list of probability tables.
     */
    public UserInterface(ArrayList<Variable> vs, ArrayList<Table> ps) {
        this.vs = vs;
        this.ps = ps;
    }

    /**
     * Asks for a query from the user.
     */
    public void askForQuery() {
        System.out.println("\nWhich variable(s) do you want to query? Please enter in the number of the variable.");
        for (int i = 0; i < vs.size(); i++) {
            System.out.println("Variable " + i + ": " + vs.get(i).getName());
        }
        scan = new Scanner(System.in);
        line = scan.nextLine();
        if (line.isEmpty()) {
            System.out.println("You have not chosen a query value. Please choose a query value.");
            askForQuery();
        }
        try {
            int queriedVar = Integer.parseInt(line);
            if (queriedVar >= 0 && queriedVar < vs.size()) {
                query = vs.get(queriedVar);
            } else {
                System.out.println("This is not a correct index. Please choose an index between " + 0 + " and "
                    + (vs.size() - 1) + ".");
                askForQuery();
            }
        } catch (NumberFormatException ex) {
            System.out.println("This is not a correct index. Please choose an index between " + 0 + " and "
                + (vs.size() - 1) + ".");
            askForQuery();
        }
    }

    /**
     * Ask the user for observed variables in the network.
     */
    public void askForObservedVariables() {

        obs.clear();
        System.out.println("Which variable(s) do you want to observe? Please enter in the number of the variable, \n"
            + "followed by a comma and the value of the observed variable. Do not use spaces. \n"
            + "If you want to query multiple variables, delimit them with a ';' and no spaces.\n"
            + "Example: '2,True;3,False'");
        for (int i = 0; i < vs.size(); i++) {
            StringBuilder values = new StringBuilder();
            for (int j = 0; j < vs.get(i).getNumberOfValues() - 1; j++) {
                values.append(vs.get(i).getValues().get(j)).append(", ");
            }
            values.append(vs.get(i).getValues().get(vs.get(i).getNumberOfValues() - 1));

            System.out.println("Variable " + i + ": " + vs.get(i).getName() + " - " + values);
        }
        scan = new Scanner(System.in);
        line = scan.nextLine();
        if (!line.isEmpty()) {
            if (!line.contains(",")) {
                System.out.println("You did not enter a comma between values. Please try again");
                askForObservedVariables();
            } else {
                while (line.contains(";")) { // Multiple observed variables
                    try {
                        int queriedVar = Integer.parseInt(line.substring(0, line.indexOf(",")));
                        String bool = line.substring(line.indexOf(",") + 1, line.indexOf(";"));
                        changeVariableToObserved(queriedVar, bool);
                        line = line.substring(line.indexOf(";") + 1); // Continue
                        // with
                        // next
                        // observed
                        // variable.
                    } catch (NumberFormatException ex) {
                        System.out.println("This is not a correct input. Please choose an index between " + 0 + " and "
                            + (vs.size() - 1) + ".");
                        askForObservedVariables();
                        return;
                    }
                }
                if (!line.contains(";")) { // Only one observed variable
                    try {

                        int queriedVar = Integer.parseInt(line.substring(0, line.indexOf(",")));
                        String bool = line.substring(line.indexOf(",") + 1);
                        changeVariableToObserved(queriedVar, bool);
                    } catch (NumberFormatException ex) {
                        System.out.println("This is not a correct input. Please choose an index between " + 0 + " and "
                            + (vs.size() - 1) + ".");
                        askForObservedVariables();
                    }
                }
            }
        }
    }

    /**
     * Checks whether a number and value represent a valid observed value or not and if so, adds it to the
     * observed list. If not, asks again for new input.
     */
    public void changeVariableToObserved(int queriedVar, String value) {
        Variable ob;
        if (queriedVar >= 0 && queriedVar < vs.size()) {
            ob = vs.get(queriedVar);
            if (ob.isValueOf(value)) {
                ob.setObservedValue(value);
                ob.setObserved(true);
            } else {
                System.out.println("Apparently you did not fill in the value correctly. You typed: \"" + value
                    + "\"Please try again");
                askForObservedVariables();
                return;
            }
            obs.add(ob); // Adding observed variable and it's value to list.
        } else {
            System.out.println("You have chosen an incorrect index. Please choose an index between " + 0 + " and "
                + (vs.size() - 1));
            askForObservedVariables();
        }
    }

    /**
     * Print the network that was read-in (by printing the variables, parents and probabilities).
     */
    public void printNetwork() {
        System.out.println("The variables:");
        for (int i = 0; i < vs.size(); i++) {
            StringBuilder values = new StringBuilder();
            for (int j = 0; j < vs.get(i).getNumberOfValues() - 1; j++) {
                values.append(vs.get(i).getValues().get(j)).append(", ");
            }
            values.append(vs.get(i).getValues().get(vs.get(i).getNumberOfValues() - 1));
            System.out.println((i + 1) + ") " + vs.get(i).getName() + " - " + values); // Printing
            // the
            // variables.
        }
        System.out.println("\nThe probabilities:");
        for (int i = 0; i < ps.size(); i++) {
            if (vs.get(i).getNrOfParents() == 1) {
                System.out.println(ps.get(i).getVariable().getName() + " has parent "
                    + vs.get(i).getParents().get(0).getName());
            } else if (vs.get(i).getNrOfParents() > 1) {
                StringBuilder parentsList = new StringBuilder();
                for (int j = 0; j < vs.get(i).getParents().size(); j++) {
                    parentsList.append(vs.get(i).getParents().get(j).getName());
                    if (!(j == vs.get(i).getParents().size() - 1)) {
                        parentsList.append(" and ");
                    }
                }
                System.out.println(ps.get(i).getVariable().getName() + " has parents "
                    + parentsList);
            } else {
                System.out.println(ps.get(i).getVariable().getName() + " has no parents.");
            }

            Table probs = ps.get(i);
            for (int l = 0; l < probs.size(); l++) {
                System.out.println(probs.get(l));      // Printing
            }                        // the
            System.out.println();              // probabilities.
        }
    }

    /**
     * Prints the query and observed variables given in by the user.
     */
    public void printQueryAndObserved(Variable query, ArrayList<Variable> Obs) {
        System.out.println("\nThe queried variable(s) is/are: "); // Printing
        // the
        // queried
        // variables.
        System.out.println(query.getName());
        if (!Obs.isEmpty()) {
            System.out.println("The observed variable(s) is/are: "); // Printing
            // the
            // observed
            // variables.
            for (Variable Ob : Obs) {
                System.out.println(Ob.getName());
                System.out.println("This variable has the value: " + Ob.getObservedValue());
            }
        }
    }

    /**
     * Asks for a heuristic.
     */
    public void askForHeuristic() {
        System.out.println("Supply a heuristic. Input 1 for least-incoming, 2 for fewest-factors and enter for random");
        scan = new Scanner(System.in);
        line = scan.nextLine();
        if (line.isEmpty()) {
            heuristic = "empty";
            System.out.println("You have chosen for random");
        } else if (line.equals("1")) {
            heuristic = "least-incoming";
            System.out.println("You have chosen for least-incoming");
        } else if (line.equals("2")) {
            heuristic = "fewest-factors";
            System.out.println("You have chosen for fewest-factors");
        } else {
            System.out.println(line + " is not an option. Please try again");
            askForHeuristic();
        }
        scan.close();
    }

    /**
     * Getter of the observed variables.
     *
     * @return a list of observed variables given by the user.
     */
    public ArrayList<Variable> getObservedVariables() {
        return obs;
    }

    /**
     * Getter of the queried variables.
     *
     * @return the variable the user wants to query.
     */
    public Variable getQueriedVariable() {
        return query;
    }

    /**
     * Getter of the heuristic.
     *
     * @return the name of the heuristic.
     */
    public String getHeuristic() {
        return heuristic;
    }

    /**
     * Prints the answer of the user query.
     *
     * @param finalTable a string of the table of the last factor after elimination
     */
    public void printQueryAnswer(String finalTable) {
        System.out.println(finalTable);
    }

    /**
     * Prints the reduced product formula based on the network structure
     *
     * @param vars a list of all variables
     */
    public void printProductFormula(ArrayList<Variable> vars) {
        StringBuilder sb = new StringBuilder();
        sb.append("\nThe reduced formula based on the network structure:\n");
        for (Variable var : vars) {
            sb.append("P(").append(var.getName(), 0, 1);
            if (var.isObserved()) {
                String value = var.getObservedValue();
                sb.append("=").append(value);
            }
            if (var.getNrOfParents() != 0) {
                sb.append("|");
                for (Variable parent : var.getParents()) {
                    sb.append(parent.getName(), 0, 1);
                    if (parent.isObserved()) {
                        String value = parent.getObservedValue();
                        sb.append("=").append(value);
                    }
                    if (var.getParents().indexOf(parent) != var.getParents().size() - 1) {
                        sb.append(",");
                    }
                }
            }
            sb.append(")");
            if (vars.indexOf(var) != vars.size() - 1) {
                sb.append("·");
            }
        }
        sb.append("\n");
        System.out.println(sb.toString());
    }

    /**
     * Prints the formula of the reduced factors.
     *
     * @param fullyObserved reduced factor list
     */
    public void printFactorFormula(ArrayList<Factor> fullyObserved, boolean firstTime) {
        StringBuilder sb = new StringBuilder();
        if (firstTime) {
            sb.append("The formula of the reduced factors:\n");
        } else {
            sb.append("Formula after the merge:\n");
        }
        addFactorsToStringBuilder(fullyObserved, sb, false);
        System.out.println(sb.toString());
    }

    /**
     * Print the elimination order.
     *
     * @param elimOrder a list of variables in the elimination order
     */
    public void printEliminationOrder(PriorityQueue<Variable> elimOrder) {
        StringBuilder sb = new StringBuilder();
        sb.append("The elimination order, based on least incoming arcs:\n");
        int order = 1;
        while (!elimOrder.isEmpty()) {
            Variable var = elimOrder.poll();
            sb.append(order).append(". ").append(var.getName(), 0, 1).append("\n");
            order++;
        }
        System.out.println(sb.toString());
    }

    /**
     * Prints the factors that are going to be merged.
     *
     * @param concerningFactors the list of factors to merge
     * @param eliminate the variable to eliminate
     */
    public void printMergingFactors(ArrayList<Factor> concerningFactors, Variable eliminate) {
        StringBuilder sb = new StringBuilder();
        if (concerningFactors.size() > 1) {
            sb.append("Merging the following factors by eliminating ").append(eliminate.getName(), 0, 1).append(" variable\n");
            addFactorsToStringBuilder(concerningFactors, sb, true);
        } else {
            sb.append("The following factor is safely ignored:\n");
            addFactorsToStringBuilder(concerningFactors, sb, true);
        }
        System.out.println(sb.toString());
    }

    /**
     * Prints the last factors that are merged. Only used if the query variable is the ancestor of the observed variable(s).
     *
     * @param factors a list of factors to be merged
     */
    public void printLastFactorMerge(ArrayList<Factor> factors) {
        StringBuilder sb = new StringBuilder();
        sb.append("Merging last remaining factors: \n");
        addFactorsToStringBuilder(factors, sb, true);
        System.out.println(sb.toString());
    }

    /**
     * An auxiliary function to add factors list to the string builder.
     *
     * @param factors a list of factors
     * @param sb string builder
     * @param newLine a check if a new line is needed
     */
    private void addFactorsToStringBuilder(ArrayList<Factor> factors, StringBuilder sb, boolean newLine) {
        for (int i = 0; i < factors.size(); i++) {
            sb.append("f").append(i + 1).append("(");
            for (Variable var : factors.get(i).getVariables()) {
                if (var.isObserved()) {
                    sb.append(var.getName(), 0, 1).append("=").append(var.getObservedValue());
                } else {
                    sb.append(var.getName(), 0, 1);
                }

                if (factors.get(i).getVariables().indexOf(var) != factors.get(i).getVariables().size() - 1) {
                    sb.append(",");
                }
            }
            sb.append(")");
            if (i != factors.size() - 1 && !newLine) {
                sb.append("·");
            } else {
                sb.append("\n");
            }
        }
    }
}