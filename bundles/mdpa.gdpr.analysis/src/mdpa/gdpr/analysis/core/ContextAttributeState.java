package mdpa.gdpr.analysis.core;

import org.eclipse.jdt.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class models a state for a {@link mdpa.gdpr.analysis.dfd.DFDGDPRTransposeFlowGraph} that has selected the stored {@link ContextDependentAttributeScenario}
 */
public record ContextAttributeState(List<ContextDependentAttributeScenario> selectedScenarios) {
    /**
     * Creates a new {@link ContextAttributeState} using the given list of selected {@link ContextDependentAttributeScenario}
     *
     * @param selectedScenarios List of selected {@link ContextDependentAttributeScenario}
     */
    public ContextAttributeState(List<ContextDependentAttributeScenario> selectedScenarios) {
        this.selectedScenarios = new ArrayList<>(selectedScenarios);
    }

    /**
     * Returns the selected {@link ContextDependentAttributeScenario} that are selected by the {@link ContextAttributeState}
     *
     * @return Returns selected {@link ContextDependentAttributeScenario}
     */
    @Override
    public List<ContextDependentAttributeScenario> selectedScenarios() {
        return Collections.unmodifiableList(this.selectedScenarios);
    }

    /**
     * Create all possible {@link ContextAttributeState} that are possible to create from the given list of {@link ContextDependentAttributeSource}
     *
     * @param contextDependentAttributeSources Given list of {@link ContextDependentAttributeSource} that are used in finding all {@link ContextAttributeState}
     * @return Returns a list of all possible {@link ContextAttributeState}
     */
    public static List<ContextAttributeState> createAllContextAttributeStates(
            List<ContextDependentAttributeSource> contextDependentAttributeSources) {
        List<List<ContextDependentAttributeScenario>> scenarios = new ArrayList<>();
        for (ContextDependentAttributeSource source : contextDependentAttributeSources) {
            scenarios.add(new ArrayList<>(source.getContextDependentAttributeScenarios()));
        }
        List<List<ContextDependentAttributeScenario>> cartesianProduct = cartesianProduct(scenarios);
        return cartesianProduct.stream()
                .map(ContextAttributeState::new)
                .toList();
    }

    /**
     * Calculates the cartesian product between the given lists
     *
     * @param lists List of lists that should be used when calculating the cartesian product
     * @param <T>   Type of the list elements
     * @return Returns the cartesian product of the provided lists
     */
    private static <T> List<List<T>> cartesianProduct(List<List<T>> lists) {
        List<List<T>> result = new ArrayList<>();
        if (lists == null || lists.isEmpty()) {
            result.add(new ArrayList<>());
            return result;
        }

        List<T> firstList = lists.get(0);
        List<List<T>> remainingLists = cartesianProduct(lists.subList(1, lists.size()));

        for (T element : firstList) {
            for (List<T> remainingList : remainingLists) {
                List<T> temp = new ArrayList<>();
                temp.add(element);
                temp.addAll(remainingList);
                result.add(temp);
            }
        }

        return result;
    }

    @Override
    @NonNull
    public String toString() {
        String scenarios = this.selectedScenarios.stream()
                .map(ContextDependentAttributeScenario::getName)
                .collect(Collectors.joining(","));
        return "[" + scenarios + "]";
    }
}
