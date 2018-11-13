package com.theappsolutions.nanostream.util.trasform;

import org.apache.beam.sdk.transforms.Combine;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Combine collection of {@link T} objects into single {@link Iterable<T>}
 * @param <T> type of input values
 */
public class CombineIterableAccumulatorFn<T> extends Combine.CombineFn<T, List<T>, Iterable<T>> {

    @Override
    public List<T> createAccumulator() {
        return new ArrayList<>();
    }

    @Override
    public List<T> addInput(List<T> accumulator, T input) {
        accumulator.add(input);
        return accumulator;
    }

    @Override
    public List<T> mergeAccumulators(Iterable<List<T>> accumulators) {
        return StreamSupport.stream(accumulators.spliterator(), false)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    @Override
    public Iterable<T> extractOutput(List<T> accumulator) {
        return accumulator;
    }
}
