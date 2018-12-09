package varelim;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Class to represent a variable.
 *
 * @author Marcel de Korte, Moira Berens, Djamari Oetringer, Abdullahi Ali, Leonieke van den Bulk
 * @co-author/editor Mantas Makelis, David Leeftink
 */
public class Variable {

    private String name;
    private ArrayList<String> possibleValues;
    private ArrayList<Variable> parents; // Note that parents is not set in the constructor, but manually set with
	// setParents() because of the .bif file layout
    private String observedValue;
    private boolean observed = false;

    /**
     * Constructor of the class.
     *
     * @param name, name of the variable.
     * @param possibleValues, the possible values of the variable.
     */
    public Variable(String name, ArrayList<String> possibleValues) {
        this.name = name;
        this.possibleValues = possibleValues;
    }

    public Variable() {}

    /**
     * Transform variable and its values to string.
     */
    public String toString() {
        StringBuilder valuesString = new StringBuilder();
        for (int i = 0; i < possibleValues.size() - 1; i++) {
            valuesString.append(possibleValues.get(i)).append(", ");
        }
        valuesString.append(possibleValues.get(possibleValues.size() - 1));
        return name + " - " + valuesString;
    }

    /**
     * Getter of the values.
     *
     * @return the values of the variable as a ArrayList of Strings.
     */
    public ArrayList<String> getValues() {
        return possibleValues;
    }

    /**
     * Check if string v is a value of the variable.
     *
     * @return a boolean denoting if possibleValues contains string v.
     */
    public boolean isValueOf(String v) {
        return possibleValues.contains(v);
    }

    /**
     * Getter of the amount of possible values of the variable.
     *
     * @return the amount of values as an int.
     */
    public int getNumberOfValues() {
        return possibleValues.size();
    }

    /**
     * Getter of the name of the variable.
     *
     * @return the name as a String.
     */
    public String getName() {
        return name;
    }

    /**
     * Getter of the parents of the variable.
     *
     * @return the list of parents as an ArrayList of Variables.
     */
    public ArrayList<Variable> getParents() {
        return Objects.requireNonNullElseGet(parents, ArrayList::new);
    }

    /**
     * Setter of the parents of the variable.
     *
     * @param parents the list of parents as an ArrayList of Variables.
     */
    public void setParents(ArrayList<Variable> parents) {
        this.parents = parents;
    }

    /**
     * Check if a variable has parents.
     *
     * @return a boolean denoting if the variable has parents.
     */
    public boolean hasParents() {
        return parents != null;
    }

    /**
     * Getter for the number of parents a variable has.
     *
     * @return the amount of parents as an int.
     */
    public int getNrOfParents() {
        if (parents != null) {
            return parents.size();
        }
        return 0;
    }

    /**
     * Setter of the observed value of the variable.
     *
     * @param observedValue as a String to which observed value the current value of the variable should be set.
     */
    public void setObservedValue(String observedValue) {
        this.observedValue = observedValue;
    }

    /**
     * Getter of the observed value of the variable.
     *
     * @return the value of the variable as a String.
     */
    public String getObservedValue() {
        return observedValue;
    }

    /**
     * Setter for if a variable is observed.
     *
     * @param observed a boolean denoting if the variable is observed or not.
     */
    public void setObserved(boolean observed) {
        this.observed = observed;
    }

    /**
     * Getter for if a variable is observed.
     *
     * @return a boolean denoting if the variable is observed or not.
     */
    public boolean isObserved() {
        return observed;
    }

    @Override
    public boolean equals(Object var) {
        Variable variable = (Variable) var;
        return name.equals(variable.getName());
    }
}