package com.wustzdy.springboot.flowable.demo.demo;


/**
 * @author xielianjun
 */
public class Edge extends GraphElement {
    private Node src;
    private Node dest;

    public Node getSrc() {
        return src;
    }

    public void setSrc(Node src) {
        this.src = src;
    }

    public Node getDest() {
        return dest;
    }

    public void setDest(Node dest) {
        this.dest = dest;
    }
}