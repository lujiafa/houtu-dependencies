package io.github.lujiafa.houtu.springcloud.loadbalancer.support.weight;

import org.springframework.cloud.client.ServiceInstance;

import java.util.*;

class LazyWeightedServiceInstanceList extends AbstractList<ServiceInstance> {

    /* for testing */ final ServiceInstance[] expanded;

    private final Object expandingLock = new Object();

    private WeightedServiceInstanceSelector selector;

    private volatile int position = 0;

    LazyWeightedServiceInstanceList(List<ServiceInstance> instances, int[] weights) {
        // Calculate the greatest common divisor (GCD) of weights, and the
        // total number of elements after expansion.
        int greatestCommonDivisor = 0;
        int total = 0;
        for (int weight : weights) {
            greatestCommonDivisor = greatestCommonDivisor(greatestCommonDivisor, weight);
            total += weight;
        }
        expanded = new ServiceInstance[total / greatestCommonDivisor];
        selector = new WeightedServiceInstanceSelector(instances, weights, greatestCommonDivisor);
    }

    @Override
    public ServiceInstance get(int index) {
        if (index >= position) {
            synchronized (expandingLock) {
                for (; position <= index && position < expanded.length; position++) {
                    expanded[position] = selector.next();
                }
                if (position == expanded.length) {
                    selector = null; // for gc
                }
            }
        }
        return expanded[index];
    }

    @Override
    public int size() {
        return expanded.length;
    }

    static int greatestCommonDivisor(int a, int b) {
        int r;
        while (b != 0) {
            r = a % b;
            a = b;
            b = r;
        }
        return a;
    }

    static class WeightedServiceInstanceSelector {

        Queue<Entry> active;

        Queue<Entry> expired;

        WeightedServiceInstanceSelector(List<ServiceInstance> instances, int[] weights, int greatestCommonDivisor) {
            active = new ArrayDeque<>(instances.size());
            expired = new ArrayDeque<>(instances.size());
            // Use iterator for some implementation of the List that not supports
            // RandomAccess, but `weights` is supported, so use a local variable `i`
            // to get the current position.
            int i = 0;
            for (ServiceInstance instance : instances) {
                active.offer(new Entry(instance, weights[i] / greatestCommonDivisor));
                i++;
            }
        }

        ServiceInstance next() {
            if (active.isEmpty()) {
                Queue<Entry> temp = active;
                active = expired;
                expired = temp;
            }

            Entry entry = active.poll();
            if (entry == null) {
                // Suppress warnings, never touched!
                return null;
            }

            entry.remainder--;
            if (entry.remainder == 0) {
                entry.remainder = entry.weight;
                expired.offer(entry);
            }
            else {
                active.offer(entry);
            }
            return entry.instance;
        }

        static class Entry {

            final ServiceInstance instance;

            final int weight;

            int remainder;

            Entry(ServiceInstance instance, int weight) {
                this.instance = instance;
                this.weight = weight;
                remainder = weight;
            }

        }

    }

}