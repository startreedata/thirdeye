/*
 * Copyright 2024 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
package ai.startree.thirdeye.aspect.utils;


public class DeltaQueue<T> {
    private static class Node<T> {
        public final T value;
        public long delay;
        public Node<T> next = null;
        
        public Node(T value, long nanos) {
            this.value = value;
            this.delay = nanos;
        }
    }
    
    private Node<T> head = null;
    
    public boolean isEmpty() {
        return head == null;
    }
    
    public boolean isNotEmpty() {
        return !isEmpty();
    }
    
    public T next() {
        return head.value;
    }

    public long delay() {
        return head.delay;
    }
    
    public long delay(T element) {
        long ret = 0;
        Node<T> next = head;
        while (next != null) {
            ret += next.delay;
            if (next.value.equals(element)) {
                break;
            }
            next = next.next;
        }
        if (next == null) {
            return -1;
        }
        return ret;
    }

    public void add(long delay, T value) {
        Node<T> newNode = new Node<T>(value, delay);
        
        Node<T> prev = null;
        Node<T> next = head;
        
        while (next != null && next.delay <= newNode.delay) {
            newNode.delay -= next.delay;
            prev = next;
            next = next.next;
        }
        
        if (prev == null) {
            head = newNode;
        }
        else {
            prev.next = newNode;
        }
        
        if (next != null) {
            next.delay -= newNode.delay;
            
            newNode.next = next;
        }
    }


    public long tick(long timeUnits) {
        if (head == null) {
            return 0L;
        }
        else if (head.delay >= timeUnits) {
            head.delay -= timeUnits;
            return 0L;
        }
        else {
            long leftover = timeUnits - head.delay;
            head.delay = 0L;
            return leftover;
        }
    }

    public T pop() {
        if (head.delay > 0) {
            throw new IllegalStateException("cannot pop the head element when it has a non-zero delay");
        }
        
        T popped = head.value;
        head = head.next;
        return popped;
    }
    
    public boolean remove(T element) {
        Node<T> prev = null;
        Node<T> node = head;
        while (node != null && node.value != element) {
            prev = node;
            node = node.next;
        }
        
        if (node == null) {
            return false;
        }
        
        if (node.next != null) {
            node.next.delay += node.delay;
        }
        
        if (prev == null) {
            head = node.next;
        }
        else {
            prev.next = node.next;
        }
        
        return true;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName())
          .append("[");
        
        Node<T> node = head;
        while (node != null) {
            if (node != head) {
                sb.append(", ");
            }
            sb.append("+")
              .append(node.delay)
              .append(": ")
              .append(node.value);
            
            node = node.next;
        }
        sb.append("]");
        
        return sb.toString();
    }
}
