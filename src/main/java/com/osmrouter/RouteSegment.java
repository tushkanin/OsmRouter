package com.osmrouter;

import java.util.List;

/**
 * User: Aleksey.Shulga
 * Date: 25.08.13
 * Time: 13:20
 */
public class RouteSegment {
    public List<Double[]> points;
    public int distance;

    public RouteSegment(List<Double[]> points, int distance) {
        this.points = points;
        this.distance = distance;
    }
}
