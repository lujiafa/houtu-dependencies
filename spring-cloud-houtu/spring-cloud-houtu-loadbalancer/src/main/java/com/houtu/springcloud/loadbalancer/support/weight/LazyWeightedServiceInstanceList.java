package com.houtu.springcloud.loadbalancer.support.weight;

import org.springframework.cloud.client.ServiceInstance;

import java.util.*;

class LazyWeightedServiceInstanceList extends AbstractList<ServiceInstance> {
    final ServiceInstance[] expanded;
    private final Object expandingLock = new Object();
    private WeightedServiceInstanceSelector selector;
    private volatile int position = 0;

    LazyWeightedServiceInstanceList(List<ServiceInstance> instances, int[] weights) {
        int greatestCommonDivisor = 0;
        int total = 0;
        int[] var5 = weights;
        int var6 = weights.length;

        for(int var7 = 0; var7 < var6; ++var7) {
            int weight = var5[var7];
            greatestCommonDivisor = greatestCommonDivisor(greatestCommonDivisor, weight);
            total += weight;
        }

        this.expanded = new ServiceInstance[total / greatestCommonDivisor];
        this.selector = new WeightedServiceInstanceSelector(instances, weights, greatestCommonDivisor);
    }

    public ServiceInstance get(int index) {
        if (index >= this.position) {
            synchronized(this.expandingLock) {
                while(this.position <= index && this.position < this.expanded.length) {
                    this.expanded[this.position] = this.selector.next();
                    ++this.position;
                }

                if (this.position == this.expanded.length) {
                    this.selector = null;
                }
            }
        }

        return this.expanded[index];
    }

    public int size() {
        return this.expanded.length;
    }

    static int greatestCommonDivisor(int a, int b) {
        while(b != 0) {
            int r = a % b;
            a = b;
            b = r;
        }

        return a;
    }

    static class WeightedServiceInstanceSelector {
        Queue<Entry> active;
        Queue<Entry> expired;

        WeightedServiceInstanceSelector(List<ServiceInstance> instances, int[] weights, int greatestCommonDivisor) {
            this.active = new ArrayDeque(instances.size());
            this.expired = new ArrayDeque(instances.size());
            int i = 0;

            for(Iterator var5 = instances.iterator(); var5.hasNext(); ++i) {
                ServiceInstance instance = (ServiceInstance)var5.next();
                this.active.offer(new Entry(instance, weights[i] / greatestCommonDivisor));
            }

        }

        ServiceInstance next() {
            if (this.active.isEmpty()) {
                Queue<Entry> temp = this.active;
                this.active = this.expired;
                this.expired = temp;
            }

            Entry entry = (Entry)this.active.poll();
            if (entry == null) {
                return null;
            } else {
                --entry.remainder;
                if (entry.remainder == 0) {
                    entry.remainder = entry.weight;
                    this.expired.offer(entry);
                } else {
                    this.active.offer(entry);
                }

                return entry.instance;
            }
        }

        static class Entry {
            final ServiceInstance instance;
            final int weight;
            int remainder;

            Entry(ServiceInstance instance, int weight) {
                this.instance = instance;
                this.weight = weight;
                this.remainder = weight;
            }
        }
    }
}
