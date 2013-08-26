package com.osmrouter;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.util.shapes.GHPlace;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Aleksey.Shulga
 * Date: 25.08.13
 * Time: 12:55
 */
public class Router {
    private GraphHopper hopper;
    private String algo;

    public Router(GraphHopper hopper, String algo) {
        this.hopper = hopper;
        this.algo = algo;
    }

    public List<RouteSegment> makeRoute(List<List<Double>> points) throws Exception{
        List<RouteSegment> route = new ArrayList<RouteSegment>();
        for (int i = 0; i < points.size() - 1; i++) {
            List<Double> currentPoint = points.get(i);
            List<Double> nextPoint = points.get(i + 1);
            if(currentPoint.size()!=2 || nextPoint.size()!=2){
                throw new Exception("Point should have lan and lng");
            }
            GHPlace from = new GHPlace(currentPoint.get(0), currentPoint.get(1));
            GHPlace to = new GHPlace(nextPoint.get(0), nextPoint.get(1));
            GHResponse rsp = hopper.route(new GHRequest(from, to)
                    .setAlgorithm(algo));
            route.add(rsp.isFound() ? new RouteSegment(rsp.getPoints().toGeoJson(), (int) rsp.getDistance())
                    : new RouteSegment(new ArrayList<Double[]>(), 0));

        }
        return route;
    }
}
