package mdpa.gdpr.analysis.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ContextAttributeState {
    private final List<ContextDependentAttributeScenario> selectedScenarios;

    public ContextAttributeState(List<ContextDependentAttributeScenario> selectedScenarios) {
        this.selectedScenarios = new ArrayList<>(selectedScenarios);
    }

    public ContextAttributeState(ContextDependentAttributeScenario... selectedScenarios) {
        this(List.of(selectedScenarios));
    }

    public List<ContextDependentAttributeSource> getContextAttributeSources() {
        return this.selectedScenarios.stream()
                .map(ContextDependentAttributeScenario::getContextDependentAttributeSource)
                .toList();
    }

    public List<ContextDependentAttributeScenario> getSelectedScenarios() {
        return Collections.unmodifiableList(this.selectedScenarios);
    }

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
    public String toString() {
        String scenarios = this.selectedScenarios.stream()
                .map(it -> it.getName())
                .collect(Collectors.joining(","));
        return "[" + scenarios + "]";
    }
}
